package com.kosmos.android.ui.designsystem.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosMotion
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.ElderTypography
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled

@Composable
fun VerdictRow(
    verdict: Verdict,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elderMode: Boolean = false,
) {
    val accent = Color(verdict.accentColor)
    val colors = KosmosThemeExt.colors
    val reduceMotion = rememberReduceMotionEnabled()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(KosmosShape.card)
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
            .then(
                if (reduceMotion) Modifier else Modifier.animateContentSize(
                    animationSpec = KosmosMotion.contentSizeSpec(),
                ),
            )
            .semantics {
                contentDescription = "${verdict.title}. ${verdict.detail}"
            },
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(KosmosDimens.accentBarWidth)
                .fillMaxHeight()
                .background(accent),
        )

        Text(
            text = verdict.emoji,
            modifier = Modifier
                .width(KosmosDimens.emojiColumnWidth)
                .padding(top = KosmosDimens.cardPaddingV),
            style = MaterialTheme.typography.titleMedium,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 10.dp, bottom = 10.dp, end = KosmosDimens.cardPaddingH),
        ) {
            Text(
                text = verdict.title,
                style = if (elderMode) ElderTypography.verdictTitle else KosmosTextStyles.verdictTitle,
                color = colors.textPrimary,
            )
            AnimatedVisibility(
                visible = expanded || elderMode,
                enter = if (reduceMotion) fadeIn() else fadeIn(KosmosMotion.fadeSpec()),
                exit = if (reduceMotion) fadeOut() else fadeOut(KosmosMotion.fadeSpec()),
            ) {
                Text(
                    text = verdict.detail,
                    style = if (elderMode) ElderTypography.verdictDetail else KosmosTextStyles.verdictDetail,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun VerdictRowPreview() {
    KosmosTheme {
        VerdictRow(
            verdict = Verdict(
                id = "raincoat",
                emoji = "☔",
                title = "Take a raincoat",
                detail = "78% rain · 4–6 PM",
                priority = VerdictPriority.SEVERE,
                accentColor = 0xFF1A5CB3,
            ),
            expanded = true,
            onClick = {},
        )
    }
}
