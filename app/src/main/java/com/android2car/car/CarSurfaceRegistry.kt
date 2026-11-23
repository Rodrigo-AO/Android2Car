package com.android2car.car

import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CarDisplaySurface(
    val surface: Surface,
    val width: Int,
    val height: Int,
    val densityDpi: Int,
)

/** Hands the car display surface from the Android Auto session to the projection service. */
class CarSurfaceRegistry {
    private val current = MutableStateFlow<CarDisplaySurface?>(null)

    val surface: StateFlow<CarDisplaySurface?> = current.asStateFlow()

    fun publish(surface: CarDisplaySurface) {
        current.value = surface
    }

    fun clear() {
        current.value = null
    }
}
