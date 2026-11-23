package com.android2car.service

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import com.android2car.car.CarDisplaySurface

/** Mirrors the phone's default display into whichever car surface is currently available. */
class ScreenMirror(
    private val projection: MediaProjection,
    private val onProjectionStopped: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var virtualDisplay: VirtualDisplay? = null
    private var released = false

    private val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            if (!released) onProjectionStopped()
        }
    }

    init {
        // Android 14 throws from createVirtualDisplay() unless a callback was registered first.
        projection.registerCallback(callback, handler)
    }

    fun renderTo(target: CarDisplaySurface?) {
        check(!released) { "ScreenMirror already released" }
        val display = virtualDisplay
        when {
            target == null -> display?.surface = null
            display == null -> virtualDisplay = createVirtualDisplay(target)
            else -> retarget(display, target)
        }
    }

    fun release() {
        if (released) return
        released = true
        virtualDisplay?.release()
        virtualDisplay = null
        projection.unregisterCallback(callback)
        projection.stop()
    }

    private fun createVirtualDisplay(target: CarDisplaySurface): VirtualDisplay =
        checkNotNull(
            projection.createVirtualDisplay(
                DISPLAY_NAME,
                target.width,
                target.height,
                target.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                target.surface,
                null,
                handler,
            ),
        ) { "MediaProjection refused to create the virtual display" }

    // Android 14 allows a single createVirtualDisplay() per MediaProjection token, so when the car
    // recreates its surface (app switch, rotation) the existing display is re-targeted instead.
    private fun retarget(display: VirtualDisplay, target: CarDisplaySurface) {
        display.resize(target.width, target.height, target.densityDpi)
        display.surface = target.surface
    }

    private companion object {
        const val DISPLAY_NAME = "Android2Car-Mirror"
    }
}
