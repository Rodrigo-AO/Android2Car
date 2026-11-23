package com.android2car.car

import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer

class CarSurfaceCallback(private val registry: CarSurfaceRegistry) : SurfaceCallback {

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        val surface = surfaceContainer.surface ?: return
        registry.publish(
            CarDisplaySurface(
                surface = surface,
                width = surfaceContainer.width,
                height = surfaceContainer.height,
                densityDpi = surfaceContainer.dpi,
            ),
        )
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        registry.clear()
    }
}
