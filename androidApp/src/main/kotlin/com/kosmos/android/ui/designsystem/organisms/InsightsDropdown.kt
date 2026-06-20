package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.i18n.S
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.prototype.PrototypeData
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosMotion
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled

@Composable
fun InsightsDropdown(
    verdicts: List<Verdict>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCopyShare: ((Verdict) -> Unit)? = null,
    onWhatsAppShare: ((Verdict) -> Unit)? = null,
    onImageShare: ((Verdict) -> Unit)? = null,
    onRemind: ((Verdict) -> Unit)? = null,
    remindedIds: Set<String> = emptySet(),
    currentHour: Int = 0,
    modifier: Modifier = Modifier,
) {
    val visibleVerdicts = verdicts.filter { it.priority != VerdictPriority.NORMAL }
        .ifEmpty { verdicts }
    if (visibleVerdicts.isEmpty()) return

    val topVerdict = visibleVerdicts.first()
    val extraCount = visibleVerdicts.size - 1
    val colors = KosmosThemeExt.colors
    val reduceMotion = rememberReduceMotionEnabled()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(KosmosShape.card)
            .background(colors.cardBackground)
            .then(
                if (reduceMotion) Modifier else Modifier.animateContentSize(
                    animationSpec = KosmosMotion.contentSizeSpec(),
                ),
            )
            .clickable(onClick = onToggle)
            .semantics {
                contentDescription = if (expanded) {
                    "Insights expanded, ${visibleVerdicts.size} items"
                } else {
                    "Insights collapsed, tap to expand"
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KosmosDimens.cardPaddingH, vertical = KosmosDimens.cardPaddingV),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!expanded) {
                Text(
                    text = topVerdict.emoji,
                    fontSize = 22.sp,
                    modifier = Modifier.width(36.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (expanded) "Today's insights" else topVerdict.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                )
                Text(
                    text = when {
                        expanded -> "${visibleVerdicts.size} insights · morning to evening"
                        extraCount > 0 -> "+$extraCount more · tap to see all"
                        else -> "Tap for details"
                    },
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse insights" else "Expand insights",
                tint = KosmosColor.primary,
                modifier = Modifier.size(24.dp),
            )
        }

        if (expanded) {
            AnimatedVisibility(
                visible = true,
                enter = if (reduceMotion) fadeIn() else slideInVertically { it / 4 } + fadeIn(),
            ) {
                Column {
                    HorizontalDivider(color = colors.border)
                    Column(
                        modifier = Modifier.padding(horizontal = KosmosDimens.cardPaddingH, vertical = KosmosDimens.grid),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        visibleVerdicts.forEach { verdict ->
                            InsightRow(
                                verdict = verdict,
                                onRemind = onRemind,
                                isReminded = verdict.id in remindedIds,
                                currentHour = currentHour,
                            )
                        }
                        if ((onCopyShare != null || onWhatsAppShare != null || onImageShare != null) && visibleVerdicts.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                onCopyShare?.let { copy ->
                                    Text(
                                        text = "Copy",
                                        color = KosmosColor.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { copy(visibleVerdicts.first()) },
                                    )
                                }
                                onWhatsAppShare?.let { wa ->
                                    Text(
                                        text = "WhatsApp",
                                        color = KosmosColor.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { wa(visibleVerdicts.first()) },
                                    )
                                }
                                onImageShare?.let { image ->
                                    Text(
                                        text = "Share card",
                                        color = KosmosColor.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { image(visibleVerdicts.first()) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightRow(
    verdict: Verdict,
    onRemind: ((Verdict) -> Unit)? = null,
    isReminded: Boolean = false,
    currentHour: Int = 0,
) {
    val colors = KosmosThemeExt.colors
    val window = verdict.timeWindow
    val canRemind = onRemind != null && window != null && window.startHour > currentHour

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(KosmosDimens.accentBarWidth)
                .background(Color(verdict.accentColor), KosmosShape.accentBar)
                .padding(vertical = 16.dp),
        )
        Text(
            text = verdict.emoji,
            fontSize = 18.sp,
            modifier = Modifier
                .width(32.dp)
                .padding(start = KosmosDimens.grid, top = 2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = verdict.title,
                style = KosmosTextStyles.verdictTitle,
                color = colors.textPrimary,
            )
            Text(
                text = verdict.detail,
                style = KosmosTextStyles.verdictDetail,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (canRemind) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(KosmosShape.timelineChip)
                        .clickable { onRemind?.invoke(verdict) }
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = if (isReminded) "🔔 ${S.get("reminder_set")}" else "⏰ ${S.get("remind_me")}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isReminded) colors.textMuted else KosmosColor.primary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun InsightsDropdownPreview() {
    KosmosTheme {
        InsightsDropdown(
            verdicts = PrototypeData.hyderabadSummerDay.verdicts,
            expanded = true,
            onToggle = {},
        )
    }
}
