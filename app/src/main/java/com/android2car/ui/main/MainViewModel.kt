package com.android2car.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android2car.di.AppContainer
import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.repository.CarConnectionRepository
import com.android2car.domain.repository.ThemeRepository
import com.android2car.domain.usecase.CheckCarConnectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val checkCarConnection: CheckCarConnectionUseCase,
    carConnectionRepository: CarConnectionRepository,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val isChecking = MutableStateFlow(false)
    private val alert = MutableStateFlow<UserAlert?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        themeRepository.themeMode,
        carConnectionRepository.status.onStart { emit(CarConnectionStatus.DISCONNECTED) },
        isChecking,
        alert,
    ) { themeMode, connection, checking, currentAlert ->
        MainUiState(
            themeMode = themeMode,
            status = resolveStatus(connection, checking, currentAlert),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MainUiState())

    fun onScreenShareClicked() {
        if (isChecking.value) return
        viewModelScope.launch { verifyCarConnection() }
    }

    fun onDarkThemeToggled(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setThemeMode(ThemeMode.fromDarkFlag(enabled)) }
    }

    private suspend fun verifyCarConnection() {
        alert.value = null
        isChecking.value = true
        val connection = try {
            checkCarConnection()
        } finally {
            isChecking.value = false
        }
        if (!connection.supportsProjection) alert.value = UserAlert.CAR_NOT_CONNECTED
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    checkCarConnection = container.checkCarConnection,
                    carConnectionRepository = container.carConnectionRepository,
                    themeRepository = container.themeRepository,
                )
            }
        }
    }
}
