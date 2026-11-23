package com.android2car.domain.model

sealed interface VehicleMotion {
    /** Signed speed as reported by the car; negative values mean the car is reversing. */
    data class Speed(val metersPerSecond: Float) : VehicleMotion

    data object Unknown : VehicleMotion
}
