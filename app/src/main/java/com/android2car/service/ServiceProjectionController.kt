package com.android2car.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.ScreenCaptureConsent
import com.android2car.domain.repository.ProjectionController
import kotlinx.coroutines.flow.StateFlow

class ServiceProjectionController(
    context: Context,
    private val store: ProjectionStateStore,
) : ProjectionController {

    private val appContext = context.applicationContext

    override val state: StateFlow<ProjectionState> = store.state

    override fun start(consent: ScreenCaptureConsent) {
        require(consent is MediaProjectionConsent) { "Unsupported consent type: ${consent::class.java.name}" }
        store.update(ProjectionState.Starting)
        ContextCompat.startForegroundService(appContext, ScreenProjectionService.startIntent(appContext, consent))
    }

    override fun stop() {
        appContext.stopService(Intent(appContext, ScreenProjectionService::class.java))
    }
}
