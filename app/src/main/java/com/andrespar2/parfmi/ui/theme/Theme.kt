package com.andrespar2.parfmi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema de ParFMI. Solo existe variante oscura: la app esta pensada para
 * pantallas AMOLED y el diseno no contempla modo claro.
 */
private val ParFmiDarkColorScheme = darkColorScheme(
    primary = ParfmiColors.Red,
    onPrimary = Color.White,
    primaryContainer = ParfmiColors.RedDark,
    onPrimaryContainer = Color.White,
    secondary = ParfmiColors.TextSecondary,
    onSecondary = ParfmiColors.AmoledBlack,
    secondaryContainer = ParfmiColors.SurfaceElevated,
    onSecondaryContainer = ParfmiColors.TextPrimary,
    tertiary = ParfmiColors.Info,
    onTertiary = ParfmiColors.AmoledBlack,
    background = ParfmiColors.AmoledBlack,
    onBackground = ParfmiColors.TextPrimary,
    surface = ParfmiColors.AmoledBlack,
    onSurface = ParfmiColors.TextPrimary,
    surfaceVariant = ParfmiColors.Surface,
    onSurfaceVariant = ParfmiColors.TextSecondary,
    surfaceContainer = ParfmiColors.Surface,
    surfaceContainerHigh = ParfmiColors.SurfaceAlt,
    outline = ParfmiColors.Border,
    outlineVariant = ParfmiColors.Border,
    error = ParfmiColors.StatusNegative,
    onError = Color.White,
    errorContainer = ParfmiColors.RedDark,
    onErrorContainer = Color.White,
    scrim = Color.Black,
)

@Composable
fun ParFmiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ParFmiDarkColorScheme,
        typography = ParFmiTypography,
        content = content,
    )
}
