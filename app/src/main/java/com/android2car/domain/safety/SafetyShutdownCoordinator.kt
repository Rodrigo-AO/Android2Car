package com.android2car.domain.safety

import com.android2car.domain.repository.ProjectionController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Stops the projection and tells every open surface (phone and car) to close. */
class SafetyShutdownCoordinator(private val projection: ProjectionController) {

    private val shutdowns = MutableSharedFlow<ShutdownReason>(extraBufferCapacity = 1)

    val events: SharedFlow<ShutdownReason> = shutdowns.asSharedFlow()

    fun shutdown(reason: ShutdownReason) {
        projection.stop()
        shutdowns.tryEmit(reason)
    }
}
