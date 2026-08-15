package com.kosmos.android.ui.designsystem.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object KosmosShape {
    val card: Shape = RoundedCornerShape(KosmosDimens.cardRadius)
    val chip: Shape = RoundedCornerShape(KosmosDimens.chipRadius)
    val timelineChip: Shape = RoundedCornerShape(KosmosDimens.timelineChipRadius)
    val fab: Shape = CircleShape
    val accentBar: Shape = RoundedCornerShape(2.dp)
    val pill: Shape = RoundedCornerShape(KosmosDimens.chipRadius)
}
