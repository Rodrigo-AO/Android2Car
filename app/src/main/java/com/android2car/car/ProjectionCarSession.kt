package com.android2car.car

import android.content.Intent
import androidx.car.app.AppManager
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android2car.di.AppContainer

class ProjectionCarSession(private val container: AppContainer) : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        attachCarSurface()
        return ProjectionCarScreen(carContext, container.projectionController)
    }

    private fun attachCarSurface() {
        val registry = container.carSurfaceRegistry
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(CarSurfaceCallback(registry))
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) = registry.clear()
            },
        )
    }
}
