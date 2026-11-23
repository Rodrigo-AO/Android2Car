package com.android2car.testing

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.repository.CarConnectionRepository
import com.android2car.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeCarConnectionRepository(initial: CarConnectionStatus? = null) : CarConnectionRepository {
    private val current = MutableStateFlow(initial)

    override val status: Flow<CarConnectionStatus> = current.filterNotNull()

    fun emit(status: CarConnectionStatus) {
        current.value = status
    }
}

class FakeThemeRepository(initial: ThemeMode? = null) : ThemeRepository {
    private val current = MutableStateFlow(initial)

    override val themeMode: Flow<ThemeMode?> = current

    val savedMode: ThemeMode? get() = current.value

    override suspend fun setThemeMode(mode: ThemeMode) {
        current.value = mode
    }
}
