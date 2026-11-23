package com.android2car.ui.main

import app.cash.turbine.test
import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.model.ProjectionError
import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.ThemeMode
import com.android2car.domain.usecase.CheckCarConnectionUseCase
import com.android2car.testing.FakeCarConnectionRepository
import com.android2car.testing.FakeConsent
import com.android2car.testing.FakeProjectionController
import com.android2car.testing.FakeThemeRepository
import com.android2car.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val connection = FakeCarConnectionRepository(CarConnectionStatus.DISCONNECTED)
    private val projection = FakeProjectionController()
    private val theme = FakeThemeRepository()

    private fun TestScope.createViewModel(): MainViewModel =
        MainViewModel(
            checkCarConnection = CheckCarConnectionUseCase(connection),
            carConnectionRepository = connection,
            projectionController = projection,
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
    fun `warns and does not request capture when the car is not connected`() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onScreenShareClicked()

            expectNoEvents()
        }
        assertEquals(ScreenShareStatus.Alert(UserAlert.CAR_NOT_CONNECTED), viewModel.uiState.value.status)
    }

    @Test
    fun `requests screen capture when Android Auto is connected`() = runTest {
        connection.emit(CarConnectionStatus.ANDROID_AUTO)
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onScreenShareClicked()

            assertEquals(MainEffect.RequestScreenCapture, awaitItem())
        }
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
    fun `starts projection with the granted consent`() = runTest {
        val viewModel = createViewModel()

        viewModel.onScreenCaptureResult(FakeConsent)

        assertEquals(listOf(FakeConsent), projection.startedWith)
        assertTrue(viewModel.uiState.value.isSharing)
        assertEquals(ScreenShareStatus.WaitingForCarDisplay, viewModel.uiState.value.status)
    }

    @Test
    fun `warns when the capture permission is denied`() = runTest {
        val viewModel = createViewModel()

        viewModel.onScreenCaptureResult(null)

        assertTrue(projection.startedWith.isEmpty())
        assertEquals(ScreenShareStatus.Alert(UserAlert.CAPTURE_PERMISSION_DENIED), viewModel.uiState.value.status)
    }

    @Test
    fun `pressing the button while sharing stops the projection`() = runTest {
        projection.emit(ProjectionState.Projecting)
        val viewModel = createViewModel()

        viewModel.onScreenShareClicked()

        assertEquals(1, projection.stopCount)
        assertFalse(viewModel.uiState.value.isSharing)
    }

    @Test
    fun `reflects projection progress in the status`() = runTest {
        val viewModel = createViewModel()

        projection.emit(ProjectionState.Projecting)
        assertEquals(ScreenShareStatus.Projecting, viewModel.uiState.value.status)

        projection.emit(ProjectionState.Failed(ProjectionError.MIRROR_FAILED))
        assertEquals(ScreenShareStatus.Alert(UserAlert.PROJECTION_FAILED), viewModel.uiState.value.status)
    }

    @Test
    fun `persists the selected theme mode`() = runTest {
        val viewModel = createViewModel()

        viewModel.onDarkThemeToggled(true)

        assertEquals(ThemeMode.DARK, theme.savedMode)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }
}
