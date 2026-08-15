package com.kosmos.android.ui.designsystem.tokens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KosmosExtendedColors(
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val bgSubtle: Color,
    val bgPill: Color,
    val accent: Color,
    val temp: Color,
)

val LocalKosmosColors = staticCompositionLocalOf {
    KosmosExtendedColors(
        cardBackground = KosmosColor.bgSurface,
        textPrimary = KosmosColor.textPrimary,
        textSecondary = KosmosColor.textSecondary,
        textMuted = KosmosColor.textTertiary,
        border = KosmosColor.border,
        bgSubtle = KosmosColor.bgSubtle,
        bgPill = KosmosColor.bgPill,
        accent = KosmosColor.accent,
        temp = KosmosColor.temp,
    )
}

object KosmosThemeExt {
    val colors: KosmosExtendedColors
        @Composable get() = LocalKosmosColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = KosmosColor.accent,
    onPrimary = Color.White,
    surface = KosmosColor.bgSurface,
    onSurface = KosmosColor.textPrimary,
    surfaceVariant = KosmosColor.bgSubtle,
    onSurfaceVariant = KosmosColor.textSecondary,
    background = KosmosColor.bgSurface,
    onBackground = KosmosColor.textPrimary,
    outline = KosmosColor.border,
)

private val DarkColorScheme = darkColorScheme(
    primary = KosmosColor.accent,
    onPrimary = Color.White,
    background = KosmosColor.bgSurfaceDark,
    onBackground = KosmosColor.textPrimaryDark,
    surface = KosmosColor.bgSurfaceDark,
    onSurface = KosmosColor.textPrimaryDark,
    surfaceVariant = KosmosColor.bgSubtleDark,
    onSurfaceVariant = KosmosColor.textSecondaryDark,
    outline = KosmosColor.borderDark,
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
            bgSubtle = if (darkTheme) Color(0xFF000000) else Color(0xFFFFFFFF),
            bgPill = if (darkTheme) Color(0xFF111111) else Color(0xFFEDEBE9),
            accent = if (darkTheme) Color.White else Color.Black,
            temp = if (darkTheme) Color.White else Color.Black,
        )
        darkTheme -> KosmosExtendedColors(
            cardBackground = KosmosColor.bgSurfaceDark,
            textPrimary = KosmosColor.textPrimaryDark,
            textSecondary = KosmosColor.textSecondaryDark,
            textMuted = KosmosColor.textTertiaryDark,
            border = KosmosColor.borderDark,
            bgSubtle = KosmosColor.bgSubtleDark,
            bgPill = KosmosColor.bgPillDark,
            accent = KosmosColor.accent,
            temp = KosmosColor.temp,
        )
        else -> KosmosExtendedColors(
            cardBackground = KosmosColor.bgSurface,
            textPrimary = KosmosColor.textPrimary,
            textSecondary = KosmosColor.textSecondary,
            textMuted = KosmosColor.textTertiary,
            border = KosmosColor.border,
            bgSubtle = KosmosColor.bgSubtle,
            bgPill = KosmosColor.bgPill,
            accent = KosmosColor.accent,
            temp = KosmosColor.temp,
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
