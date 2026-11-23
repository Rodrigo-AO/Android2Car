package com.android2car.service

import android.content.Context
import android.os.PowerManager
import kotlin.time.Duration.Companion.hours

/** Keeps the CPU running while the phone screen is off so frames keep reaching the car. */
class WakeLockHolder(context: Context) {

    private val wakeLock: PowerManager.WakeLock = context.getSystemService(PowerManager::class.java)
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        .apply { setReferenceCounted(false) }

    fun acquire() {
        // Bounded so that a missed release can never keep the device awake indefinitely.
        wakeLock.acquire(MAX_HOLD.inWholeMilliseconds)
    }

    fun release() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    private companion object {
        const val TAG = "Android2Car:ScreenProjection"
        val MAX_HOLD = 4.hours
    }
}
