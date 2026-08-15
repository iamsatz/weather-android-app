package com.kosmos.android.ui.designsystem.atoms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kosmos.android.R
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles

@Composable
fun ActivityChip(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    Row(
        modifier = modifier
            .clip(KosmosShape.chip)
            .background(background)
            .padding(horizontal = KosmosDimens.sm, vertical = KosmosDimens.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = KosmosColor.textPrimary,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            style = KosmosTextStyles.compactLabel,
            color = KosmosColor.textPrimary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityChipRow(
    showWalk: Boolean,
    showExercise: Boolean,
    showOutdoor: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!showWalk && !showExercise && !showOutdoor) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KosmosDimens.sm),
        verticalArrangement = Arrangement.spacedBy(KosmosDimens.sm),
    ) {
        if (showWalk) {
            ActivityChip("Best for walk", KosmosColor.chipWalk, icon = R.drawable.ic_person_simple_walk)
        }
        if (showExercise) {
            ActivityChip("Exercise / Yoga", KosmosColor.chipExercise, icon = R.drawable.ic_person_simple_tai_chi)
        }
        if (showOutdoor) {
            ActivityChip("Outdoor Activity", KosmosColor.chipOutdoor, icon = R.drawable.ic_person_simple_walk)
        }
    }
}
