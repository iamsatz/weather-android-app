package com.kosmos.android.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.android.ui.designsystem.atoms.kosmosGlossyGlass

@Composable
fun NowcastCard(
    message: String,
    updatedMinutesAgo: Int,
    isWet: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = KosmosThemeExt.colors

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .kosmosGlossyGlass()
            .padding(horizontal = KosmosDimens.cardPaddingH, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(KosmosColor.nowcastAccent),
        )
        Text(
            text = if (isWet) "🌧" else "📍",
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 10.dp, end = 4.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        ) {
            Text(
                text = message,
                style = KosmosTextStyles.verdictDetail.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.textPrimary,
                lineHeight = 18.sp,
            )
            Text(
                text = buildString {
                    append("● ")
                    append(
                        when {
                            updatedMinutesAgo <= 0 -> "Updated just now"
                            updatedMinutesAgo == 1 -> "Updated 1 min ago"
                            else -> "Updated $updatedMinutesAgo min ago"
                        },
                    )
                },
                style = KosmosTextStyles.settingsSubtitle.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.03.sp,
                ),
                color = KosmosColor.nowcastAccent,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

@Preview
@Composable
private fun NowcastCardPreview() {
    KosmosTheme {
        NowcastCard(
            message = "Rain likely in your area (Biramguda) in about an hour — carry a raincoat if you head out.",
            updatedMinutesAgo = 2,
            isWet = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
