package com.android2car.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android2car.appContainer
import com.android2car.domain.model.ThemeMode
import com.android2car.ui.main.MainScreen
import com.android2car.ui.main.MainViewModel
import com.android2car.ui.theme.Android2CarTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.factory(appContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun applySystemBarStyle(darkTheme: Boolean) {
        val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
