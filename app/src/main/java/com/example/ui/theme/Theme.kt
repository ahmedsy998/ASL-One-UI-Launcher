package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.model.ThemeMode

@Composable
fun ASLOneUiLauncherTheme(
    themeMode: ThemeMode = ThemeMode.ONE_UI_DARK_GLASS,
    accentHex: String = "#6366F1",
    fontFamilyName: String = "Cairo",
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val accentColor = parseHexColor(accentHex, OneUiPrimaryBlue)

    val colorScheme = when (themeMode) {
        ThemeMode.ONE_UI_LIGHT -> lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.16f),
            onPrimaryContainer = TextPrimaryLight,
            secondary = OneUiElectricPurple,
            onSecondary = Color.White,
            background = OneUiLightBg,
            onBackground = TextPrimaryLight,
            surface = OneUiLightSurface,
            onSurface = TextPrimaryLight,
            surfaceVariant = OneUiLightCard,
            onSurfaceVariant = TextSecondaryLight
        )

        ThemeMode.AMOLED_BLACK -> darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = OneUiVioletGlow,
            onSecondary = Color.White,
            background = AmoledBlackBg,
            onBackground = TextPrimaryDark,
            surface = AmoledSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = AmoledCard,
            onSurfaceVariant = TextSecondaryDark
        )

        ThemeMode.ONE_UI_DARK_GLASS -> darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = OneUiVioletGlow,
            onSecondary = Color.White,
            background = OneUiDarkBgStart,
            onBackground = TextPrimaryDark,
            surface = OneUiGlassSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = OneUiGlassCard,
            onSurfaceVariant = TextSecondaryDark
        )
    }

    val typography = buildLauncherTypography(
        fontName = fontFamilyName,
        fontScale = fontScale
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    ASLOneUiLauncherTheme(content = content)
}
