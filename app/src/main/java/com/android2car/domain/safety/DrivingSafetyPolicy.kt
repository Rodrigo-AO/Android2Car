package com.android2car.domain.safety

import com.android2car.domain.model.VehicleMotion
import kotlin.math.absoluteValue

enum class ShutdownReason {
    VEHICLE_IN_MOTION,
    SPEED_UNAVAILABLE,
}

sealed interface SafetyVerdict {
    data object Safe : SafetyVerdict
    data class Unsafe(val reason: ShutdownReason) : SafetyVerdict
}

/**
 * Projection is only allowed while the car is stationary.
 *
 * @param stopOnUnknownSpeed when true a car that cannot report its speed is treated as moving.
 * @param stationaryToleranceMps speeds at or below this magnitude still count as stationary.
 */
class DrivingSafetyPolicy(
    private val stopOnUnknownSpeed: Boolean,
    private val stationaryToleranceMps: Float = 0f,
) {
    init {
        require(stationaryToleranceMps >= 0f) { "Tolerance must not be negative" }
    }

    fun evaluate(motion: VehicleMotion): SafetyVerdict = when (motion) {
        is VehicleMotion.Speed -> evaluateSpeed(motion.metersPerSecond)
        VehicleMotion.Unknown -> if (stopOnUnknownSpeed) unsafe(ShutdownReason.SPEED_UNAVAILABLE) else SafetyVerdict.Safe
    }

    private fun evaluateSpeed(metersPerSecond: Float): SafetyVerdict =
        if (metersPerSecond.absoluteValue > stationaryToleranceMps) {
            unsafe(ShutdownReason.VEHICLE_IN_MOTION)
        } else {
            SafetyVerdict.Safe
        }

    private fun unsafe(reason: ShutdownReason) = SafetyVerdict.Unsafe(reason)
}
