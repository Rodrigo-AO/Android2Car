package com.android2car.domain.safety

import com.android2car.domain.model.VehicleMotion
import org.junit.Assert.assertEquals
import org.junit.Test

class DrivingSafetyPolicyTest {

    private val strictPolicy = DrivingSafetyPolicy(stopOnUnknownSpeed = true)
    private val lenientPolicy = DrivingSafetyPolicy(stopOnUnknownSpeed = false)

    @Test
    fun `stationary car is safe`() {
        assertEquals(SafetyVerdict.Safe, strictPolicy.evaluate(VehicleMotion.Speed(0f)))
    }

    @Test
    fun `any forward speed above zero is unsafe`() {
        assertEquals(
            SafetyVerdict.Unsafe(ShutdownReason.VEHICLE_IN_MOTION),
            strictPolicy.evaluate(VehicleMotion.Speed(0.01f)),
        )
    }

    @Test
    fun `reversing counts as moving`() {
        assertEquals(
            SafetyVerdict.Unsafe(ShutdownReason.VEHICLE_IN_MOTION),
            lenientPolicy.evaluate(VehicleMotion.Speed(-1.5f)),
        )
    }

    @Test
    fun `speeds within the tolerance count as stationary`() {
        val policy = DrivingSafetyPolicy(stopOnUnknownSpeed = true, stationaryToleranceMps = 0.2f)

        assertEquals(SafetyVerdict.Safe, policy.evaluate(VehicleMotion.Speed(0.2f)))
        assertEquals(SafetyVerdict.Unsafe(ShutdownReason.VEHICLE_IN_MOTION), policy.evaluate(VehicleMotion.Speed(0.21f)))
    }

    @Test
    fun `unknown speed is unsafe when the policy is fail-safe`() {
        assertEquals(
            SafetyVerdict.Unsafe(ShutdownReason.SPEED_UNAVAILABLE),
            strictPolicy.evaluate(VehicleMotion.Unknown),
        )
    }

    @Test
    fun `unknown speed is tolerated when the policy is lenient`() {
        assertEquals(SafetyVerdict.Safe, lenientPolicy.evaluate(VehicleMotion.Unknown))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative tolerance is rejected`() {
        DrivingSafetyPolicy(stopOnUnknownSpeed = true, stationaryToleranceMps = -1f)
    }
}
