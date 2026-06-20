package com.kosmos.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun KosmosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    com.kosmos.android.ui.designsystem.tokens.KosmosTheme(
        darkTheme = darkTheme,
        content = content,
    )
}
