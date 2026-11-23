package dev.ghostbyte.nowandagain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = LightHighest,
    onPrimaryContainer = Ink,
    secondary = LightVariantText,
    onSecondary = Paper,
    secondaryContainer = LightHighest,
    onSecondaryContainer = Ink,
    tertiary = LightVariantText,
    onTertiary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = LightHigh,
    onSurfaceVariant = LightVariantText,
    surfaceContainerLowest = LightLowest,
    surfaceContainerLow = LightLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightHigh,
    surfaceContainerHighest = LightHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Red40,
    onError = Paper,
    onErrorContainer = Ink,
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = Paper,
)

val DarkColorScheme = darkColorScheme(
    primary = SoftWhite,
    onPrimary = Charcoal,
    primaryContainer = DarkHighest,
    onPrimaryContainer = SoftWhite,
    secondary = DarkVariantText,
    onSecondary = Charcoal,
    secondaryContainer = DarkHighest,
    onSecondaryContainer = SoftWhite,
    tertiary = DarkVariantText,
    onTertiary = Charcoal,
    background = Charcoal,
    onBackground = SoftWhite,
    surface = Charcoal,
    onSurface = SoftWhite,
    surfaceVariant = DarkHigh,
    onSurfaceVariant = DarkVariantText,
    surfaceContainerLowest = DarkLowest,
    surfaceContainerLow = DarkLow,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkHigh,
    surfaceContainerHighest = DarkHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Red80,
    onError = Charcoal,
    onErrorContainer = SoftWhite,
    inverseSurface = SoftWhite,
    inverseOnSurface = Charcoal,
    inversePrimary = Charcoal,
)

@Composable
fun NowAndAgainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalStatusColors provides if (darkTheme) DarkStatus else LightStatus
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}