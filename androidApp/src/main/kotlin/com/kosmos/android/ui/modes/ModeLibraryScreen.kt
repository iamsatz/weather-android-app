package com.kosmos.android.ui.modes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.i18n.S
import com.kosmos.android.ui.designsystem.molecules.ModeCardData
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.mode.UserMode

private const val MAX_ADDED_MODES = 5
private const val MAX_ACTIVE_LENSES = 2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeLibraryScreen(
    modeCards: List<ModeCardData>,
    comingSoonCards: List<ModeCardData>,
    addedModeIds: Set<String>,
    activeModeIds: List<String>,
    onToggleActive: (String) -> Unit,
    onTakeoverConfirm: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onModeInfo: (ModeCardData) -> Unit = {},
) {
    val defaultId = UserMode.DEFAULT.id
    val liveBrowse = modeCards.filter { it.id != defaultId && it.id !in addedModeIds && !it.isComingSoon }
    val yourModes = modeCards.filter { it.id == defaultId || it.id in addedModeIds }
    val atCap = addedModeIds.size >= MAX_ADDED_MODES
    val colors = KosmosThemeExt.colors
    var comingSoonExpanded by remember { mutableStateOf(false) }
    var teaserMode by remember { mutableStateOf<ModeCardData?>(null) }
    var pendingTakeoverId by remember { mutableStateOf<String?>(null) }

    val activeLenses = activeModeIds.filter { id ->
        val mode = UserMode.fromId(id)
        mode.isStackableLens && mode != UserMode.DEFAULT
    }

    pendingTakeoverId?.let { modeId ->
        AlertDialog(
            onDismissRequest = { pendingTakeoverId = null },
            title = { Text(S.get("mode_takeover_title")) },
            text = { Text(S.get("mode_takeover_body")) },
            confirmButton = {
                TextButton(onClick = {
                    onTakeoverConfirm(modeId)
                    pendingTakeoverId = null
                }) {
                    Text(S.get("mode_takeover_confirm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingTakeoverId = null }) {
                    Text(S.get("mode_takeover_cancel"))
                }
            },
        )
    }

    teaserMode?.let { mode ->
        ModeDetailSheet(mode = mode, onDismiss = { teaserMode = null })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = S.get("modes_title"),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = "⚙",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenSettings)
                            .padding(8.dp),
                        color = colors.textSecondary,
                    )
                }
                Text(
                    text = S.get("modes_subtitle"),
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
                Text(
                    text = S.get("active_lenses_count").replace("{n}", activeLenses.size.toString()).replace("{max}", MAX_ACTIVE_LENSES.toString()),
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            item {
                SectionHeader("${S.get("your_modes")} · ${addedModeIds.size}/$MAX_ADDED_MODES")
            }
            items(yourModes, key = { it.id }) { mode ->
                val isActive = mode.id in activeModeIds
                val modeEnum = UserMode.fromId(mode.id)
                val lensCapReached = !isActive &&
                    modeEnum.isStackableLens &&
                    modeEnum != UserMode.DEFAULT &&
                    activeLenses.size >= MAX_ACTIVE_LENSES &&
                    !activeModeIds.any { UserMode.fromId(it).isTakeoverMode }

                LibraryModeCard(
                    mode = mode,
                    onInfo = { onModeInfo(mode); teaserMode = mode },
                    trailing = {
                        if (isActive) {
                            Pill(
                                text = S.get("mode_active"),
                                filled = true,
                                onClick = {
                                    if (mode.id != defaultId) onToggleActive(mode.id)
                                },
                            )
                        } else {
                            Pill(
                                text = S.get("mode_use"),
                                filled = false,
                                enabled = !lensCapReached,
                                onClick = {
                                    when {
                                        modeEnum.isTakeoverMode && activeLenses.isNotEmpty() ->
                                            pendingTakeoverId = mode.id
                                        modeEnum.isTakeoverMode ->
                                            onTakeoverConfirm(mode.id)
                                        else -> onToggleActive(mode.id)
                                    }
                                },
                            )
                        }
                    },
                    secondary = if (mode.id == defaultId) null else {
                        { onRemove(mode.id) }
                    },
                )
                Spacer(Modifier.height(8.dp))
            }

            if (liveBrowse.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    SectionHeader(S.get("available_now"))
                }
                items(liveBrowse, key = { it.id }) { mode ->
                    LibraryModeCard(
                        mode = mode,
                        dim = atCap,
                        onInfo = { onModeInfo(mode); teaserMode = mode },
                        trailing = {
                            Pill(
                                text = S.get("mode_add"),
                                filled = false,
                                enabled = !atCap,
                                onClick = { onAdd(mode.id) },
                            )
                        },
                        secondary = null,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (comingSoonCards.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .clickable { comingSoonExpanded = !comingSoonExpanded }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (comingSoonExpanded) "▼" else "▶",
                            fontSize = 14.sp,
                            color = colors.textMuted,
                        )
                        Text(
                            text = S.get("coming_soon_accordion"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        )
                    }
                }
                if (comingSoonExpanded) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            comingSoonCards.forEach { mode ->
                                ComingSoonChip(
                                    mode = mode,
                                    onClick = { teaserMode = mode },
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            if (atCap) {
                item {
                    Text(
                        text = S.get("mode_cap_reached"),
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ComingSoonChip(mode: ModeCardData, onClick: () -> Unit) {
    val colors = KosmosThemeExt.colors
    Column(
        modifier = Modifier
            .alpha(0.75f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .fillMaxWidth(0.48f),
    ) {
        Text(text = mode.emoji, fontSize = 22.sp)
        Text(
            text = mode.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = S.get("coming_soon"),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = KosmosThemeExt.colors.textMuted,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun LibraryModeCard(
    mode: ModeCardData,
    trailing: @Composable () -> Unit,
    secondary: (() -> Unit)?,
    onInfo: () -> Unit,
    dim: Boolean = false,
) {
    val colors = KosmosThemeExt.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onInfo)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = mode.emoji, fontSize = 28.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (dim) colors.textMuted else colors.textPrimary,
            )
            Text(
                text = mode.description,
                fontSize = 12.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(top = 2.dp),
            )
            secondary?.let { onRemove ->
                Text(
                    text = S.get("mode_remove"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KosmosColor.aqiUnhealthy,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable(onClick = onRemove),
                )
            }
        }
        trailing()
    }
}

@Composable
private fun Pill(
    text: String,
    filled: Boolean,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = KosmosThemeExt.colors
    val base = Modifier.clip(RoundedCornerShape(20.dp))
    val styled = if (filled) {
        base.background(KosmosColor.primary)
    } else {
        base.border(
            1.5.dp,
            if (enabled) KosmosColor.primary else colors.border,
            RoundedCornerShape(20.dp),
        )
    }
    val clickable = if (enabled && onClick != null) styled.clickable(onClick = onClick) else styled
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = when {
            !enabled -> colors.textMuted
            filled -> KosmosColor.textOnGradient
            else -> KosmosColor.primary
        },
        modifier = clickable.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
