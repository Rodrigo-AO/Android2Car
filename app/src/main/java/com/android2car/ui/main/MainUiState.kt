package com.android2car.ui.main

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ThemeMode

data class MainUiState(
    val themeMode: ThemeMode? = null,
    val status: ScreenShareStatus = ScreenShareStatus.Disconnected,
) {
    val isChecking: Boolean get() = status == ScreenShareStatus.Checking
}

sealed interface ScreenShareStatus {
    data object Disconnected : ScreenShareStatus
    data object Checking : ScreenShareStatus
    data object Connected : ScreenShareStatus
    data class Alert(val alert: UserAlert) : ScreenShareStatus
}

enum class UserAlert {
    CAR_NOT_CONNECTED,
}

internal fun resolveStatus(
    connection: CarConnectionStatus,
    isChecking: Boolean,
    alert: UserAlert?,
): ScreenShareStatus = when {
    isChecking -> ScreenShareStatus.Checking
    alert != null -> ScreenShareStatus.Alert(alert)
    connection.supportsProjection -> ScreenShareStatus.Connected
    else -> ScreenShareStatus.Disconnected
}
