package com.android2car.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android2car.R

@Composable
fun StatusCard(status: ScreenShareStatus, modifier: Modifier = Modifier) {
    val isAlert = status is ScreenShareStatus.Alert
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) colors.errorContainer else colors.surfaceContainer,
            contentColor = if (isAlert) colors.onErrorContainer else colors.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusIndicator(status)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(status.titleRes()), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(status.descriptionRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAlert) colors.onErrorContainer else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: ScreenShareStatus) {
    if (status == ScreenShareStatus.Checking) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 3.dp)
        return
    }
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(color = status.indicatorColor(), shape = CircleShape),
    )
}

@Composable
private fun ScreenShareStatus.indicatorColor(): Color {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        ScreenShareStatus.Disconnected -> colors.outline
        is ScreenShareStatus.Alert -> colors.onErrorContainer
        else -> colors.primary
    }
}

@StringRes
private fun ScreenShareStatus.titleRes(): Int = when (this) {
    ScreenShareStatus.Disconnected -> R.string.status_disconnected_title
    ScreenShareStatus.Checking -> R.string.status_checking_title
    ScreenShareStatus.Connected -> R.string.status_connected_title
    is ScreenShareStatus.Alert -> R.string.status_alert_title
}

@StringRes
private fun ScreenShareStatus.descriptionRes(): Int = when (this) {
    ScreenShareStatus.Disconnected -> R.string.status_disconnected_description
    ScreenShareStatus.Checking -> R.string.status_checking_description
    ScreenShareStatus.Connected -> R.string.status_connected_description
    is ScreenShareStatus.Alert -> alert.messageRes()
}

@StringRes
private fun UserAlert.messageRes(): Int = when (this) {
    UserAlert.CAR_NOT_CONNECTED -> R.string.alert_car_not_connected
}
