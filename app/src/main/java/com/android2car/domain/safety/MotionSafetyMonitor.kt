package com.android2car.domain.safety

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest

class MotionSafetyMonitor(
    private val source: MotionSource,
    private val policy: DrivingSafetyPolicy,
    private val unknownSpeedGracePeriod: Duration = DEFAULT_UNKNOWN_SPEED_GRACE_PERIOD,
) {
    /**
     * Suspends until projecting becomes unsafe and returns why, or `null` if the source ends.
     *
     * Movement is reported immediately. Missing speed data is only reported once it persists for
     * [unknownSpeedGracePeriod], because hosts commonly send an "unknown" sample before the first
     * real reading.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun awaitUnsafeCondition(): ShutdownReason? = source.motion
        .map(policy::evaluate)
        .distinctUntilChanged()
        .transformLatest { verdict ->
            if (verdict !is SafetyVerdict.Unsafe) return@transformLatest
            if (verdict.reason == ShutdownReason.SPEED_UNAVAILABLE) delay(unknownSpeedGracePeriod)
            emit(verdict.reason)
        }
        .firstOrNull()

    private companion object {
        val DEFAULT_UNKNOWN_SPEED_GRACE_PERIOD = 3.seconds
    }
}
