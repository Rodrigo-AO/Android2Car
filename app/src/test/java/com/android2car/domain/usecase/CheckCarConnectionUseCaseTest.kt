package com.android2car.domain.usecase

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.testing.FakeCarConnectionRepository
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckCarConnectionUseCaseTest {

    @Test
    fun `returns the current connection status`() = runTest {
        val useCase = CheckCarConnectionUseCase(FakeCarConnectionRepository(CarConnectionStatus.ANDROID_AUTO))

        assertEquals(CarConnectionStatus.ANDROID_AUTO, useCase())
    }

    @Test
    fun `reports disconnected when the car never answers`() = runTest {
        val useCase = CheckCarConnectionUseCase(FakeCarConnectionRepository(initial = null), timeout = 2.seconds)

        assertEquals(CarConnectionStatus.DISCONNECTED, useCase())
    }
}
