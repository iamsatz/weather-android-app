package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = KosmosThemeExt.colors.cardBackground,
    textColor: Color = KosmosThemeExt.colors.textPrimary,
) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = textColor,
        modifier = modifier
            .clip(KosmosShape.chip)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Preview
@Composable
private fun PillPreview() {
    KosmosTheme {
        Pill(text = "●Good", backgroundColor = KosmosColor.cardBackground)
    }
}
