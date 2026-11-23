package com.android2car.data.connection

import androidx.car.app.connection.CarConnection
import com.android2car.domain.model.CarConnectionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarConnectionTypeMapperTest {

    @Test
    fun `projection type maps to Android Auto`() {
        assertEquals(CarConnectionStatus.ANDROID_AUTO, CarConnection.CONNECTION_TYPE_PROJECTION.toCarConnectionStatus())
    }

    @Test
    fun `native type maps to Automotive OS`() {
        assertEquals(CarConnectionStatus.AUTOMOTIVE_OS, CarConnection.CONNECTION_TYPE_NATIVE.toCarConnectionStatus())
    }

    @Test
    fun `not connected and unknown types map to disconnected`() {
        assertEquals(CarConnectionStatus.DISCONNECTED, CarConnection.CONNECTION_TYPE_NOT_CONNECTED.toCarConnectionStatus())
        assertEquals(CarConnectionStatus.DISCONNECTED, 42.toCarConnectionStatus())
    }

    @Test
    fun `only Android Auto supports phone screen projection`() {
        assertTrue(CarConnectionStatus.ANDROID_AUTO.supportsProjection)
        assertFalse(CarConnectionStatus.AUTOMOTIVE_OS.supportsProjection)
        assertFalse(CarConnectionStatus.DISCONNECTED.supportsProjection)
    }
}
