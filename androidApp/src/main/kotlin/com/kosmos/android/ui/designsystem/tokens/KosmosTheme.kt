package com.kosmos.android.ui.designsystem.tokens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kosmos.android.ui.designsystem.tokens.ElderTypography

data class KosmosExtendedColors(
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
)

val LocalKosmosColors = staticCompositionLocalOf {
    KosmosExtendedColors(
        cardBackground = KosmosColor.cardBackground,
        textPrimary = KosmosColor.textPrimary,
        textSecondary = KosmosColor.textSecondary,
        textMuted = KosmosColor.textMuted,
        border = KosmosColor.border,
    )
}

object KosmosThemeExt {
    val colors: KosmosExtendedColors
        @Composable get() = LocalKosmosColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = KosmosColor.primary,
    onPrimary = Color.White,
    surface = KosmosColor.surfaceLight,
    onSurface = KosmosColor.textPrimary,
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = KosmosColor.textMuted,
)

private val DarkColorScheme = darkColorScheme(
    primary = KosmosColor.primaryLight,
    onPrimary = Color(0xFF0A0B1A),
    background = KosmosColor.surfaceDark,
    onBackground = Color(0xFFEEEEF5),
    surface = KosmosColor.surfaceDark,
    onSurface = Color(0xFFEEEEF5),
    surfaceVariant = KosmosColor.surfaceVariantDark,
    onSurfaceVariant = Color(0xFF9898B0),
)

@Composable
fun KosmosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    elderMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val extended = when {
        elderMode -> KosmosExtendedColors(
            cardBackground = if (darkTheme) Color(0xFF000000) else Color(0xFFFFFFFF),
            textPrimary = if (darkTheme) Color(0xFFFFFFFF) else Color(0xFF000000),
            textSecondary = if (darkTheme) Color(0xFFE0E0E0) else Color(0xFF1A1A1A),
            textMuted = if (darkTheme) Color(0xFFCCCCCC) else Color(0xFF333333),
            border = if (darkTheme) Color(0xFFFFFFFF) else Color(0xFF000000),
        )
        darkTheme -> KosmosExtendedColors(
            cardBackground = KosmosColor.cardBackgroundDark,
            textPrimary = Color(0xFFEEEEF5),
            textSecondary = Color(0xFFB0B0C8),
            textMuted = Color(0xFF9898B0),
            border = Color(0xFF2A2A40),
        )
        else -> KosmosExtendedColors(
            cardBackground = KosmosColor.cardBackground,
            textPrimary = KosmosColor.textPrimary,
            textSecondary = KosmosColor.textSecondary,
            textMuted = KosmosColor.textMuted,
            border = KosmosColor.border,
        )
    }

    val colorScheme = when {
        elderMode && darkTheme -> DarkColorScheme.copy(
            primary = Color.White,
            onPrimary = Color.Black,
            surface = Color.Black,
            onSurface = Color.White,
        )
        elderMode -> LightColorScheme.copy(
            primary = Color.Black,
            onPrimary = Color.White,
            surface = Color.White,
            onSurface = Color.Black,
        )
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalKosmosColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (elderMode) KosmosTypography.copy(
                displayLarge = ElderTypography.temp,
                titleMedium = ElderTypography.verdictTitle,
                bodyMedium = ElderTypography.verdictDetail,
            ) else KosmosTypography,
            content = content,
        )
    }
}
