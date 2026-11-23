package com.android2car.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

internal val LightColors = lightColorScheme(
    primary = LightBlue,
    onPrimary = DarkBlue,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,
    inversePrimary = DarkBlue,
    secondary = DarkBlue,
    onSecondary = PureWhite,
    secondaryContainer = DarkBlue,
    onSecondaryContainer = PureWhite,
    tertiary = AlertYellow,
    onTertiary = PureBlack,
    tertiaryContainer = AlertYellow,
    onTertiaryContainer = PureBlack,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = LightGray,
    onSurfaceVariant = MediumGray,
    surfaceTint = LightBlue,
    surfaceBright = PureWhite,
    surfaceDim = LightGray,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = LightGray,
    surfaceContainer = LightGray,
    surfaceContainerHigh = LightGray,
    surfaceContainerHighest = LightGray,
    inverseSurface = DarkBlue,
    inverseOnSurface = PureWhite,
    error = AlertYellow,
    onError = PureBlack,
    errorContainer = AlertYellow,
    onErrorContainer = PureBlack,
    outline = MediumGray,
    outlineVariant = LightGray,
    scrim = PureBlack,
)

internal val DarkColors = darkColorScheme(
    primary = LightBlue,
    onPrimary = DarkBlue,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,
    inversePrimary = DarkBlue,
    secondary = LightGray,
    onSecondary = DarkBlue,
    secondaryContainer = DarkBlue,
    onSecondaryContainer = PureWhite,
    tertiary = AlertYellow,
    onTertiary = PureBlack,
    tertiaryContainer = AlertYellow,
    onTertiaryContainer = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = PureBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkBlue,
    onSurfaceVariant = LightGray,
    surfaceTint = LightBlue,
    surfaceBright = DarkBlue,
    surfaceDim = PureBlack,
    surfaceContainerLowest = PureBlack,
    surfaceContainerLow = DarkBlue,
    surfaceContainer = DarkBlue,
    surfaceContainerHigh = DarkBlue,
    surfaceContainerHighest = DarkBlue,
    inverseSurface = LightGray,
    inverseOnSurface = DarkBlue,
    error = AlertYellow,
    onError = PureBlack,
    errorContainer = AlertYellow,
    onErrorContainer = PureBlack,
    outline = MediumGray,
    outlineVariant = MediumGray,
    scrim = PureBlack,
)

/** Dynamic color is intentionally not supported: the brand palette must always be used. */
@Composable
fun Android2CarTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
