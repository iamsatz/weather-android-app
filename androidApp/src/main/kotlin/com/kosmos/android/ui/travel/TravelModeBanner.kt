package com.kosmos.android.ui.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.TravelState
import com.kosmos.android.ui.designsystem.tokens.KosmosColor

@Composable
fun TravelModeBanner(
    travelState: TravelState,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!travelState.isActive) return
    val home = travelState.home ?: return

    Text(
        text = "✈️ Travel mode · ${travelState.distanceKm} km from ${home.city} · Where to go?",
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KosmosColor.primary.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        color = KosmosColor.textOnGradient,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
