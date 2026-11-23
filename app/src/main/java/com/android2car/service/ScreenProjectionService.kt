package com.android2car.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.android2car.appContainer
import com.android2car.car.CarDisplaySurface
import com.android2car.domain.model.ProjectionError
import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Owns the MediaProjection session. Running as a `mediaProjection` foreground service with a
 * partial wake lock keeps the capture alive while the phone screen is off or locked.
 */
class ScreenProjectionService : LifecycleService() {

    private val container by lazy { appContainer }
    private val stateStore by lazy { container.projectionStateStore }
    private val wakeLock by lazy { WakeLockHolder(this) }
    private var mirror: ScreenMirror? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startProjection(intent)
            else -> stopProjection(ProjectionState.Idle)
        }
        // The consent token is single-use, so a system restart could never resume the capture.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseResources()
        if (stateStore.state.value.isActive) stateStore.update(ProjectionState.Idle)
        super.onDestroy()
    }

    private fun startProjection(intent: Intent) {
        if (mirror != null) return
        // Android 14 requires the service to be in the foreground before the token is redeemed.
        if (!enterForeground()) return fail(ProjectionError.CAPTURE_UNAVAILABLE)
        val consent = MediaProjectionConsent.readFrom(intent) ?: return fail(ProjectionError.CAPTURE_UNAVAILABLE)
        val projection = obtainProjection(consent) ?: return fail(ProjectionError.CAPTURE_UNAVAILABLE)

        mirror = ScreenMirror(projection, onProjectionStopped = { stopProjection(ProjectionState.Idle) })
        wakeLock.acquire()
        stateStore.update(ProjectionState.WaitingForCarDisplay)
        mirrorCarSurface()
        stopWhenCarDisconnects()
    }

    private fun enterForeground(): Boolean = runCatching {
        ServiceCompat.startForeground(
            this,
            ProjectionNotificationFactory.NOTIFICATION_ID,
            ProjectionNotificationFactory(this).create(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
        )
    }.onFailure { Log.e(TAG, "Unable to enter the foreground", it) }.isSuccess

    private fun obtainProjection(consent: MediaProjectionConsent): MediaProjection? = runCatching {
        getSystemService(MediaProjectionManager::class.java).getMediaProjection(consent.resultCode, consent.data)
    }.onFailure { Log.e(TAG, "MediaProjection token rejected", it) }.getOrNull()

    private fun mirrorCarSurface() {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            container.carSurfaceRegistry.surface.collect(::renderOn)
        }
    }

    private fun stopWhenCarDisconnects() {
        lifecycleScope.launch {
            container.carConnectionRepository.status.first { !it.supportsProjection }
            stopProjection(ProjectionState.Idle)
        }
    }

    private fun renderOn(target: CarDisplaySurface?) {
        val activeMirror = mirror ?: return
        runCatching { activeMirror.renderTo(target) }
            .onSuccess {
                stateStore.update(if (target == null) ProjectionState.WaitingForCarDisplay else ProjectionState.Projecting)
            }
            .onFailure { error ->
                Log.e(TAG, "Unable to mirror onto the car display", error)
                fail(ProjectionError.MIRROR_FAILED)
            }
    }

    private fun fail(error: ProjectionError) = stopProjection(ProjectionState.Failed(error))

    private fun stopProjection(finalState: ProjectionState) {
        releaseResources()
        stateStore.update(finalState)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseResources() {
        mirror?.release()
        mirror = null
        wakeLock.release()
    }

    companion object {
        private const val TAG = "ScreenProjection"
        private const val ACTION_START = "com.android2car.action.START_PROJECTION"
        private const val ACTION_STOP = "com.android2car.action.STOP_PROJECTION"

        fun startIntent(context: Context, consent: MediaProjectionConsent): Intent =
            consent.writeTo(Intent(context, ScreenProjectionService::class.java).setAction(ACTION_START))

        fun stopIntent(context: Context): Intent =
            Intent(context, ScreenProjectionService::class.java).setAction(ACTION_STOP)
    }
}
