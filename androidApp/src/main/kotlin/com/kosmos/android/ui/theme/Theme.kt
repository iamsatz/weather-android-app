package com.kosmos.android.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun KosmosTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    com.kosmos.android.ui.designsystem.tokens.KosmosTheme(
        darkTheme = false,
        content = content,
    )
}
