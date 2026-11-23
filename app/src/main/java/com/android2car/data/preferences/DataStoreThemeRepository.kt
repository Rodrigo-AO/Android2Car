package com.android2car.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.repository.ThemeRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreThemeRepository(private val dataStore: DataStore<Preferences>) : ThemeRepository {

    override val themeMode: Flow<ThemeMode?> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> preferences[THEME_MODE_KEY]?.toThemeModeOrNull() }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE_KEY] = mode.name }
    }

    private fun String.toThemeModeOrNull(): ThemeMode? = ThemeMode.entries.firstOrNull { it.name == this }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
