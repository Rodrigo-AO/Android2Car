package com.android2car.car

import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.info.Speed
import com.android2car.domain.model.VehicleMotion
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedMappingTest {

    private fun value(speed: Float?, status: Int = CarValue.STATUS_SUCCESS) = CarValue(speed, 0L, status)

    private fun speed(raw: CarValue<Float>, display: CarValue<Float>): Speed =
        Speed.Builder()
            .setRawSpeedMetersPerSecond(raw)
            .setDisplaySpeedMetersPerSecond(display)
            .build()

    @Test
    fun `uses the raw speed when available`() {
        val motion = speed(raw = value(4f), display = value(5f)).toVehicleMotion()

        assertEquals(VehicleMotion.Speed(4f), motion)
    }

    @Test
    fun `falls back to the display speed when raw speed is unavailable`() {
        val motion = speed(raw = value(null, CarValue.STATUS_UNAVAILABLE), display = value(0f)).toVehicleMotion()

        assertEquals(VehicleMotion.Speed(0f), motion)
    }

    @Test
    fun `reports unknown when no reading is usable`() {
        val motion = speed(
            raw = value(null, CarValue.STATUS_UNIMPLEMENTED),
            display = value(Float.NaN),
        ).toVehicleMotion()

        assertEquals(VehicleMotion.Unknown, motion)
    }

    @Test
    fun `ignores values whose status is not success`() {
        val motion = speed(raw = value(9f, CarValue.STATUS_UNKNOWN), display = value(9f, CarValue.STATUS_UNKNOWN))
            .toVehicleMotion()

        assertEquals(VehicleMotion.Unknown, motion)
    }
}
