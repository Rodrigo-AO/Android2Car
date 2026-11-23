package com.android2car.ui.main

import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.usecase.CheckCarConnectionUseCase
import com.android2car.testing.FakeCarConnectionRepository
import com.android2car.testing.FakeThemeRepository
import com.android2car.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val connection = FakeCarConnectionRepository(CarConnectionStatus.DISCONNECTED)
    private val theme = FakeThemeRepository()

    private fun TestScope.createViewModel(): MainViewModel =
        MainViewModel(
            checkCarConnection = CheckCarConnectionUseCase(connection),
            carConnectionRepository = connection,
            themeRepository = theme,
        ).also { viewModel ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        }

    @Test
    fun `shows disconnected status when Android Auto is not connected`() = runTest {
        val viewModel = createViewModel()

        assertEquals(ScreenShareStatus.Disconnected, viewModel.uiState.value.status)
    }

    @Test
    fun `shows connected status when Android Auto is connected`() = runTest {
        val viewModel = createViewModel()

        connection.emit(CarConnectionStatus.ANDROID_AUTO)

        assertEquals(ScreenShareStatus.Connected, viewModel.uiState.value.status)
    }

    @Test
    fun `warns when screen share is pressed without a car connection`() = runTest {
        val viewModel = createViewModel()

        viewModel.onScreenShareClicked()

        assertEquals(ScreenShareStatus.Alert(UserAlert.CAR_NOT_CONNECTED), viewModel.uiState.value.status)
    }

    @Test
    fun `clears the warning once a later check succeeds`() = runTest {
        val viewModel = createViewModel()
        viewModel.onScreenShareClicked()

        connection.emit(CarConnectionStatus.ANDROID_AUTO)
        viewModel.onScreenShareClicked()

        assertEquals(ScreenShareStatus.Connected, viewModel.uiState.value.status)
    }

    @Test
    fun `persists the selected theme mode`() = runTest {
        val viewModel = createViewModel()

        viewModel.onDarkThemeToggled(true)

        assertEquals(ThemeMode.DARK, theme.savedMode)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }
}
