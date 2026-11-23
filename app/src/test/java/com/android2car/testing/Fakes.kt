package com.android2car.testing

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.ScreenCaptureConsent
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.repository.CarConnectionRepository
import com.android2car.domain.repository.ProjectionController
import com.android2car.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeCarConnectionRepository(initial: CarConnectionStatus? = null) : CarConnectionRepository {
    private val current = MutableStateFlow(initial)

    override val status: Flow<CarConnectionStatus> = current.filterNotNull()

    fun emit(status: CarConnectionStatus) {
        current.value = status
    }
}

object FakeConsent : ScreenCaptureConsent

class FakeProjectionController : ProjectionController {
    private val current = MutableStateFlow<ProjectionState>(ProjectionState.Idle)
    val startedWith = mutableListOf<ScreenCaptureConsent>()
    var stopCount = 0
        private set

    override val state: StateFlow<ProjectionState> = current

    override fun start(consent: ScreenCaptureConsent) {
        startedWith += consent
        current.value = ProjectionState.Starting
    }

    override fun stop() {
        stopCount++
        current.value = ProjectionState.Idle
    }

    fun emit(state: ProjectionState) {
        current.value = state
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
