package com.android2car.domain.usecase

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.repository.CarConnectionRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class CheckCarConnectionUseCase(
    private val repository: CarConnectionRepository,
    private val timeout: Duration = DEFAULT_TIMEOUT,
) {
    suspend operator fun invoke(): CarConnectionStatus =
        withTimeoutOrNull(timeout) { repository.status.first() } ?: CarConnectionStatus.DISCONNECTED

    private companion object {
        val DEFAULT_TIMEOUT = 3.seconds
    }
}
