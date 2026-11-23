package com.android2car.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.android2car.appContainer
import com.android2car.domain.model.ThemeMode
import com.android2car.service.MediaProjectionConsent
import com.android2car.ui.main.MainEffect
import com.android2car.ui.main.MainScreen
import com.android2car.ui.main.MainViewModel
import com.android2car.ui.theme.Android2CarTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.factory(appContainer) }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { launchScreenCaptureConsent() }

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onScreenCaptureResult(MediaProjectionConsent.fromActivityResult(result.resultCode, result.data))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectEffects()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = state.themeMode?.let { it == ThemeMode.DARK } ?: isSystemInDarkTheme()
            LaunchedEffect(darkTheme) { applySystemBarStyle(darkTheme) }
            Android2CarTheme(darkTheme = darkTheme) {
                MainScreen(
                    state = state,
                    isDarkTheme = darkTheme,
                    onScreenShareClick = viewModel::onScreenShareClicked,
                    onDarkThemeChange = viewModel::onDarkThemeToggled,
                )
            }
        }
    }

    // Collected for the whole activity lifetime (not only while started) so a driving shutdown
    // also closes the app when it sits in the background.
    private fun collectEffects() {
        lifecycleScope.launch {
            viewModel.effects.collect { effect ->
                when (effect) {
                    MainEffect.RequestScreenCapture -> requestScreenCapture()
                    MainEffect.CloseApp -> finishAndRemoveTask()
                }
            }
        }
    }

    private fun requestScreenCapture() {
        if (needsNotificationPermission()) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            launchScreenCaptureConsent()
        }
    }

    private fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED

    private fun launchScreenCaptureConsent() {
        screenCaptureLauncher.launch(createScreenCaptureIntent())
    }

    private fun createScreenCaptureIntent(): Intent {
        val manager = getSystemService(MediaProjectionManager::class.java)
        // Android 14 offers "share a single app"; the car mirror needs the whole display, so that
        // option is removed from the system consent dialog.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
        } else {
            manager.createScreenCaptureIntent()
        }
    }

    private fun applySystemBarStyle(darkTheme: Boolean) {
        val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
