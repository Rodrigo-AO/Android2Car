package com.android2car.domain.model

enum class ThemeMode {
    LIGHT,
    DARK;

    companion object {
        fun fromDarkFlag(isDark: Boolean): ThemeMode = if (isDark) DARK else LIGHT
    }
}
