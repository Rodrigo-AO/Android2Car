package com.android2car.domain.safety

import app.cash.turbine.test
import com.android2car.domain.model.ProjectionState
import com.android2car.testing.FakeProjectionController
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SafetyShutdownCoordinatorTest {

    private val projection = FakeProjectionController()
    private val coordinator = SafetyShutdownCoordinator(projection)

    @Test
    fun `stops the projection and broadcasts the reason`() = runTest {
        projection.emit(ProjectionState.Projecting)

        coordinator.events.test {
            coordinator.shutdown(ShutdownReason.VEHICLE_IN_MOTION)

            assertEquals(ShutdownReason.VEHICLE_IN_MOTION, awaitItem())
        }
        assertEquals(1, projection.stopCount)
        assertEquals(ProjectionState.Idle, projection.state.value)
    }

    @Test
    fun `stops the projection even when nobody is listening`() {
        coordinator.shutdown(ShutdownReason.SPEED_UNAVAILABLE)

        assertEquals(1, projection.stopCount)
    }
}
