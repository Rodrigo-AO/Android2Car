package com.android2car.domain.repository

import com.android2car.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    /** Emits `null` until the user picks a mode, meaning the system setting applies. */
    val themeMode: Flow<ThemeMode?>

    suspend fun setThemeMode(mode: ThemeMode)
}
