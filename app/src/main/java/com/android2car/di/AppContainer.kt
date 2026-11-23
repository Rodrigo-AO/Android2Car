package com.android2car.di

import android.content.Context
import com.android2car.data.connection.AndroidAutoConnectionRepository
import com.android2car.data.preferences.DataStoreThemeRepository
import com.android2car.data.preferences.settingsDataStore
import com.android2car.domain.repository.CarConnectionRepository
import com.android2car.domain.repository.ThemeRepository
import com.android2car.domain.usecase.CheckCarConnectionUseCase

/** Manual dependency graph shared by the phone UI, the projection service and the car session. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val themeRepository: ThemeRepository = DataStoreThemeRepository(appContext.settingsDataStore)

    val carConnectionRepository: CarConnectionRepository = AndroidAutoConnectionRepository(appContext)

    val checkCarConnection = CheckCarConnectionUseCase(carConnectionRepository)
}
