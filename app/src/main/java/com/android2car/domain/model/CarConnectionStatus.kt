package com.android2car.domain.model

enum class CarConnectionStatus {
    DISCONNECTED,
    ANDROID_AUTO,
    AUTOMOTIVE_OS;

    /** Only projected Android Auto sessions run on the phone and can mirror its screen. */
    val supportsProjection: Boolean get() = this == ANDROID_AUTO
}
