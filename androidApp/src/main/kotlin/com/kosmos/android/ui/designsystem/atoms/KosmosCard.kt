package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun Modifier.kosmosGlossyGlass(): Modifier {
    val colors = KosmosThemeExt.colors
    val dark = isSystemInDarkTheme()
    val glassBorder = if (dark) KosmosColor.glassBorderDark else KosmosColor.glassBorder
    val sheen = if (dark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }
    return this
        .fillMaxWidth()
        .shadow(
            elevation = KosmosDimens.cardShadowY + 2.dp,
            shape = KosmosShape.card,
            spotColor = Color.Black.copy(alpha = 0.12f),
            ambientColor = Color.Black.copy(alpha = 0.06f),
        )
        .clip(KosmosShape.card)
        .background(colors.cardBackground)
        .border(width = 1.dp, color = glassBorder, shape = KosmosShape.card)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(sheen, Color.Transparent),
                startY = 0f,
                endY = 120f,
            ),
        )
}

@Composable
private fun Modifier.kosmosGlassCard(): Modifier = kosmosGlossyGlass()

@Composable
fun KosmosCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var cardModifier = modifier.kosmosGlassCard()
    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }

    Column(
        modifier = cardModifier.padding(
            horizontal = KosmosDimens.cardPaddingH,
            vertical = KosmosDimens.cardPaddingV,
        ),
        content = content,
    )
}

@Composable
fun KosmosCardRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    var cardModifier = modifier.kosmosGlassCard()
    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }

    Row(
        modifier = cardModifier.padding(
            horizontal = KosmosDimens.cardPaddingH,
            vertical = KosmosDimens.cardPaddingV,
        ),
        content = content,
    )
}

@Composable
fun GlossySheenOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.35f)),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF5E89B8)
@Composable
private fun KosmosCardPreview() {
    KosmosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            KosmosCard {
                androidx.compose.material3.Text("Card content")
            }
        }
    }
}
