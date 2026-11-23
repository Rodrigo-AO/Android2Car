package com.android2car.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android2car.R
import com.android2car.ui.theme.Android2CarTheme

@Composable
fun MainScreen(
    state: MainUiState,
    isDarkTheme: Boolean,
    onScreenShareClick: () -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header(isDarkTheme = isDarkTheme, onDarkThemeChange = onDarkThemeChange)
            Spacer(modifier = Modifier.weight(1f))
            StatusCard(status = state.status)
            Spacer(modifier = Modifier.height(32.dp))
            ScreenShareButton(
                isSharing = state.isSharing,
                enabled = !state.isChecking,
                onClick = onScreenShareClick,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.main_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Header(isDarkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        DarkThemeToggle(checked = isDarkTheme, onCheckedChange = onDarkThemeChange)
    }
}

@Composable
private fun DarkThemeToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.dark_mode),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ScreenShareButton(isSharing: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        colors = if (isSharing) {
            ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.onSecondary)
        } else {
            ButtonDefaults.buttonColors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
    ) {
        Text(
            text = stringResource(if (isSharing) R.string.stop_screen_share else R.string.screen_share),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun MainScreenLightPreview() {
    Android2CarTheme(darkTheme = false) {
        MainScreen(MainUiState(status = ScreenShareStatus.Connected), false, {}, {})
    }
}

@Preview(name = "Dark – projecting", showBackground = true)
@Composable
private fun MainScreenDarkProjectingPreview() {
    Android2CarTheme(darkTheme = true) {
        MainScreen(MainUiState(status = ScreenShareStatus.Projecting, isSharing = true), true, {}, {})
    }
}

@Preview(name = "Dark – alert", showBackground = true)
@Composable
private fun MainScreenDarkAlertPreview() {
    Android2CarTheme(darkTheme = true) {
        MainScreen(MainUiState(status = ScreenShareStatus.Alert(UserAlert.CAR_NOT_CONNECTED)), true, {}, {})
    }
}
