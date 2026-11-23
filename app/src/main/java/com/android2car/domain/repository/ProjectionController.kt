package com.android2car.domain.repository

import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.ScreenCaptureConsent
import kotlinx.coroutines.flow.StateFlow

interface ProjectionController {
    val state: StateFlow<ProjectionState>

    fun start(consent: ScreenCaptureConsent)

    fun stop()
}
