package com.android2car.car

import android.util.Log
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.CarInfo
import androidx.car.app.hardware.info.Speed
import com.android2car.domain.model.VehicleMotion
import com.android2car.domain.safety.MotionSource
import java.util.concurrent.Executor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Streams vehicle speed from [CarInfo] (CarHardwareManager) as domain [VehicleMotion] samples. */
class CarSpeedMotionSource(
    private val carInfo: CarInfo,
    private val executor: Executor,
) : MotionSource {

    override val motion: Flow<VehicleMotion> = callbackFlow {
        val listener = OnCarDataAvailableListener<Speed> { speed -> trySend(speed.toVehicleMotion()) }
        val registered = runCatching { carInfo.addSpeedListener(executor, listener) }
            .onFailure { error ->
                Log.w(TAG, "Speed listener unavailable", error)
                trySend(VehicleMotion.Unknown)
            }
            .isSuccess
        awaitClose { if (registered) carInfo.removeSpeedListener(listener) }
    }

    companion object {
        const val PERMISSION = "com.google.android.gms.permission.CAR_SPEED"
        private const val TAG = "CarSpeedMotionSource"
    }
}

internal fun Speed.toVehicleMotion(): VehicleMotion {
    val metersPerSecond = rawSpeedMetersPerSecond.validValue() ?: displaySpeedMetersPerSecond.validValue()
    return metersPerSecond?.let(VehicleMotion::Speed) ?: VehicleMotion.Unknown
}

private fun CarValue<Float>.validValue(): Float? =
    value?.takeIf { status == CarValue.STATUS_SUCCESS && it.isFinite() }
