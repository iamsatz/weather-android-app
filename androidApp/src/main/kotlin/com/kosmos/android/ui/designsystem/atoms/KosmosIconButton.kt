package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

@Composable
fun KosmosIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = KosmosColor.textOnGradient,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(icon, contentDescription, tint = tint)
    }
}

@Preview
@Composable
private fun KosmosIconButtonPreview() {
    KosmosTheme {
        KosmosIconButton(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            onClick = {},
        )
    }
}
