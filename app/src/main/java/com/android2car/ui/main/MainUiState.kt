package com.android2car.ui.main

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.model.isActive

data class MainUiState(
    val themeMode: ThemeMode? = null,
    val status: ScreenShareStatus = ScreenShareStatus.Disconnected,
    val isSharing: Boolean = false,
) {
    val isChecking: Boolean get() = status == ScreenShareStatus.Checking
}

sealed interface ScreenShareStatus {
    data object Disconnected : ScreenShareStatus
    data object Checking : ScreenShareStatus
    data object Connected : ScreenShareStatus
    data object WaitingForCarDisplay : ScreenShareStatus
    data object Projecting : ScreenShareStatus
    data class Alert(val alert: UserAlert) : ScreenShareStatus
}

enum class UserAlert {
    CAR_NOT_CONNECTED,
    CAPTURE_PERMISSION_DENIED,
    PROJECTION_FAILED,
}

sealed interface MainEffect {
    data object RequestScreenCapture : MainEffect
}

internal fun resolveStatus(
    connection: CarConnectionStatus,
    projection: ProjectionState,
    isChecking: Boolean,
    alert: UserAlert?,
): ScreenShareStatus = when {
    isChecking -> ScreenShareStatus.Checking
    alert != null -> ScreenShareStatus.Alert(alert)
    projection is ProjectionState.Failed -> ScreenShareStatus.Alert(UserAlert.PROJECTION_FAILED)
    projection == ProjectionState.Projecting -> ScreenShareStatus.Projecting
    projection.isActive -> ScreenShareStatus.WaitingForCarDisplay
    connection.supportsProjection -> ScreenShareStatus.Connected
    else -> ScreenShareStatus.Disconnected
}
