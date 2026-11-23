package com.android2car.car

import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.car.app.AppManager
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.hardware.CarHardwareManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android2car.R
import com.android2car.di.AppContainer
import com.android2car.domain.safety.MotionSafetyMonitor
import com.android2car.domain.safety.ShutdownReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProjectionCarSession(private val container: AppContainer) : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        attachCarSurface()
        closeOnSafetyShutdown()
        withSpeedPermission(::startMotionSafetyMonitor)
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

    private fun withSpeedPermission(action: () -> Unit) {
        val permission = CarSpeedMotionSource.PERMISSION
        if (ContextCompat.checkSelfPermission(carContext, permission) == PackageManager.PERMISSION_GRANTED) {
            action()
            return
        }
        // Monitoring starts even if the user rejects: the policy then decides how to treat a car
        // that cannot report its speed.
        carContext.requestPermissions(listOf(permission)) { _, _ ->
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) action()
        }
    }

    private fun startMotionSafetyMonitor() {
        val carInfo = carContext.getCarService(CarHardwareManager::class.java).carInfo
        val monitor = MotionSafetyMonitor(
            source = CarSpeedMotionSource(carInfo, ContextCompat.getMainExecutor(carContext)),
            policy = container.drivingSafetyPolicy,
        )
        lifecycleScope.launch {
            monitor.awaitUnsafeCondition()?.let(container.safetyShutdownCoordinator::shutdown)
        }
    }

    private fun closeOnSafetyShutdown() {
        lifecycleScope.launch {
            val reason = container.safetyShutdownCoordinator.events.first()
            CarToast.makeText(carContext, reason.messageRes(), CarToast.LENGTH_LONG).show()
            carContext.finishCarApp()
        }
    }

    @StringRes
    private fun ShutdownReason.messageRes(): Int = when (this) {
        ShutdownReason.VEHICLE_IN_MOTION -> R.string.shutdown_vehicle_in_motion
        ShutdownReason.SPEED_UNAVAILABLE -> R.string.shutdown_speed_unavailable
    }
}
