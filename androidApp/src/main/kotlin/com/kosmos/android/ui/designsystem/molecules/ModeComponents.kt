package com.kosmos.android.ui.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.mode.CommuteMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommuteChipRow(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CommuteMode.entries.forEach { mode ->
            val isSelected = mode.id in selected
            Text(
                text = "${mode.emoji} ${mode.id.replaceFirstChar { it.uppercase() }}",
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) KosmosColor.primary else KosmosThemeExt.colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) KosmosColor.primary else KosmosThemeExt.colors.border,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable { onToggle(mode.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
fun ModeCard(
    emoji: String,
    name: String,
    description: String,
    phase: String,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        selected -> KosmosColor.primary
        locked -> KosmosThemeExt.colors.border.copy(alpha = 0.5f)
        else -> KosmosThemeExt.colors.border
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !locked, onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (locked) KosmosThemeExt.colors.textMuted else KosmosThemeExt.colors.textPrimary,
                )
                Text(
                    text = phase,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = if (locked) "🔒 Kosmos+ required" else description,
                fontSize = 12.sp,
                color = KosmosThemeExt.colors.textMuted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun ModeGrid(
    modes: List<ModeCardData>,
    selectedModeId: String,
    onModeSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        modes.forEach { mode ->
            ModeCard(
                emoji = mode.emoji,
                name = mode.name,
                description = mode.description,
                phase = mode.phase,
                selected = mode.id == selectedModeId,
                locked = mode.locked,
                onClick = { onModeSelect(mode.id) },
            )
        }
    }
}

data class ModeCardData(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
    val phase: String,
    val locked: Boolean,
    val isComingSoon: Boolean = false,
    val plannedInsights: List<String> = emptyList(),
)
