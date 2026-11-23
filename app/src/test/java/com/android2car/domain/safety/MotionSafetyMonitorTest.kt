package com.android2car.domain.safety

import com.android2car.domain.model.VehicleMotion
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MotionSafetyMonitorTest {

    private class FakeMotionSource(override val motion: Flow<VehicleMotion>) : MotionSource

    private val samples = MutableSharedFlow<VehicleMotion>()

    private fun monitor(stopOnUnknownSpeed: Boolean = true, motion: Flow<VehicleMotion> = samples) =
        MotionSafetyMonitor(
            source = FakeMotionSource(motion),
            policy = DrivingSafetyPolicy(stopOnUnknownSpeed),
            unknownSpeedGracePeriod = GRACE,
        )

    private fun TestScope.startMonitoring(monitor: MotionSafetyMonitor) =
        async { monitor.awaitUnsafeCondition() }.also { runCurrent() }

    @Test
    fun `keeps waiting while the car stays stationary`() = runTest {
        val result = startMonitoring(monitor())

        samples.emit(VehicleMotion.Speed(0f))
        samples.emit(VehicleMotion.Speed(0f))
        advanceTimeBy(1.seconds)

        assertFalse(result.isCompleted)
        result.cancel()
    }

    @Test
    fun `reports motion as soon as the car starts moving`() = runTest {
        val result = startMonitoring(monitor())

        samples.emit(VehicleMotion.Speed(0f))
        samples.emit(VehicleMotion.Speed(2.5f))
        runCurrent()

        assertTrue(result.isCompleted)
        assertEquals(ShutdownReason.VEHICLE_IN_MOTION, result.await())
    }

    @Test
    fun `ignores unknown speed that recovers within the grace period`() = runTest {
        val result = startMonitoring(monitor())

        samples.emit(VehicleMotion.Unknown)
        advanceTimeBy(GRACE - 100.milliseconds)
        samples.emit(VehicleMotion.Speed(0f))
        advanceTimeBy(GRACE * 2)

        assertFalse(result.isCompleted)
        result.cancel()
    }

    @Test
    fun `reports unavailable speed once it outlasts the grace period`() = runTest {
        val result = startMonitoring(monitor())

        samples.emit(VehicleMotion.Unknown)
        advanceTimeBy(GRACE + 1.milliseconds)

        assertTrue(result.isCompleted)
        assertEquals(ShutdownReason.SPEED_UNAVAILABLE, result.await())
    }

    @Test
    fun `motion during the grace period is reported immediately`() = runTest {
        val result = startMonitoring(monitor())

        samples.emit(VehicleMotion.Unknown)
        advanceTimeBy(100.milliseconds)
        samples.emit(VehicleMotion.Speed(1f))
        runCurrent()

        assertEquals(ShutdownReason.VEHICLE_IN_MOTION, result.await())
    }

    @Test
    fun `lenient policy never stops for unknown speed`() = runTest {
        val result = startMonitoring(monitor(stopOnUnknownSpeed = false))

        samples.emit(VehicleMotion.Unknown)
        advanceTimeBy(GRACE * 10)

        assertFalse(result.isCompleted)
        result.cancel()
    }

    @Test
    fun `returns null when the motion source ends without danger`() = runTest {
        val finiteSource = flowOf(VehicleMotion.Speed(0f), VehicleMotion.Speed(0f))

        assertNull(monitor(motion = finiteSource).awaitUnsafeCondition())
    }

    private companion object {
        val GRACE = 3.seconds
    }
}
