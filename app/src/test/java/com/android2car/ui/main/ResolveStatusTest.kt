package com.android2car.ui.main

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ProjectionError
import com.android2car.domain.model.ProjectionState
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveStatusTest {

    private fun resolve(
        connection: CarConnectionStatus = CarConnectionStatus.ANDROID_AUTO,
        projection: ProjectionState = ProjectionState.Idle,
        isChecking: Boolean = false,
        alert: UserAlert? = null,
    ) = resolveStatus(connection, projection, isChecking, alert)

    @Test
    fun `checking takes precedence over everything else`() {
        assertEquals(
            ScreenShareStatus.Checking,
            resolve(isChecking = true, alert = UserAlert.CAR_NOT_CONNECTED, projection = ProjectionState.Projecting),
        )
    }

    @Test
    fun `explicit alerts are shown before projection state`() {
        assertEquals(
            ScreenShareStatus.Alert(UserAlert.CAPTURE_PERMISSION_DENIED),
            resolve(alert = UserAlert.CAPTURE_PERMISSION_DENIED, projection = ProjectionState.Projecting),
        )
    }

    @Test
    fun `failed projection is surfaced as an alert`() {
        assertEquals(
            ScreenShareStatus.Alert(UserAlert.PROJECTION_FAILED),
            resolve(projection = ProjectionState.Failed(ProjectionError.CAPTURE_UNAVAILABLE)),
        )
    }

    @Test
    fun `starting and waiting projections wait for the car display`() {
        assertEquals(ScreenShareStatus.WaitingForCarDisplay, resolve(projection = ProjectionState.Starting))
        assertEquals(ScreenShareStatus.WaitingForCarDisplay, resolve(projection = ProjectionState.WaitingForCarDisplay))
    }

    @Test
    fun `idle projection falls back to the connection status`() {
        assertEquals(ScreenShareStatus.Connected, resolve(connection = CarConnectionStatus.ANDROID_AUTO))
        assertEquals(ScreenShareStatus.Disconnected, resolve(connection = CarConnectionStatus.DISCONNECTED))
        assertEquals(ScreenShareStatus.Disconnected, resolve(connection = CarConnectionStatus.AUTOMOTIVE_OS))
    }
}
