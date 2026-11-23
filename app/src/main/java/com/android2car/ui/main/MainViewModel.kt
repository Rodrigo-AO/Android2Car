package com.android2car.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android2car.di.AppContainer
import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ScreenCaptureConsent
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.model.isActive
import com.android2car.domain.repository.CarConnectionRepository
import com.android2car.domain.repository.ProjectionController
import com.android2car.domain.repository.ThemeRepository
import com.android2car.domain.usecase.CheckCarConnectionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val checkCarConnection: CheckCarConnectionUseCase,
    carConnectionRepository: CarConnectionRepository,
    private val projectionController: ProjectionController,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val isChecking = MutableStateFlow(false)
    private val alert = MutableStateFlow<UserAlert?>(null)
    private val effectChannel = Channel<MainEffect>(Channel.BUFFERED)

    val effects: Flow<MainEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<MainUiState> = combine(
        themeRepository.themeMode,
        carConnectionRepository.status.onStart { emit(CarConnectionStatus.DISCONNECTED) },
        projectionController.state,
        isChecking,
        alert,
    ) { themeMode, connection, projection, checking, currentAlert ->
        MainUiState(
            themeMode = themeMode,
            status = resolveStatus(connection, projection, checking, currentAlert),
            isSharing = projection.isActive,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MainUiState())

    fun onScreenShareClicked() {
        when {
            projectionController.state.value.isActive -> projectionController.stop()
            !isChecking.value -> viewModelScope.launch { requestProjectionIfConnected() }
        }
    }

    fun onScreenCaptureResult(consent: ScreenCaptureConsent?) {
        if (consent == null) {
            alert.value = UserAlert.CAPTURE_PERMISSION_DENIED
            return
        }
        projectionController.start(consent)
    }

    fun onDarkThemeToggled(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setThemeMode(ThemeMode.fromDarkFlag(enabled)) }
    }

    private suspend fun requestProjectionIfConnected() {
        alert.value = null
        isChecking.value = true
        val connection = try {
            checkCarConnection()
        } finally {
            isChecking.value = false
        }
        if (connection.supportsProjection) {
            effectChannel.send(MainEffect.RequestScreenCapture)
        } else {
            alert.value = UserAlert.CAR_NOT_CONNECTED
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    checkCarConnection = container.checkCarConnection,
                    carConnectionRepository = container.carConnectionRepository,
                    projectionController = container.projectionController,
                    themeRepository = container.themeRepository,
                )
            }
        }
    }
}
