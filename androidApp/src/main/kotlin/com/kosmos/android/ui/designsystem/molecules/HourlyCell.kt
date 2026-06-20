package com.kosmos.android.ui.designsystem.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.HourlyForecast
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

@Composable
fun HourlyCell(
    hour: HourlyForecast,
    modifier: Modifier = Modifier,
    onGradient: Boolean = true,
) {
    val labelColor = if (onGradient) {
        if (hour.isNow) KosmosColor.textOnGradient else KosmosColor.textOnGradient.copy(alpha = 0.8f)
    } else {
        Color.Unspecified
    }

    Column(
        modifier = modifier.width(KosmosDimens.hourlyCellWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = hour.label,
            style = KosmosTextStyles.hourlyTime,
            fontWeight = if (hour.isNow) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
        )
        Text(
            text = "${hour.temp}°",
            style = KosmosTextStyles.hourlyTemp,
            color = if (onGradient) KosmosColor.textOnGradient else Color.Unspecified,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        Text(text = hour.emoji, fontSize = 16.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF5E89B8)
@Composable
private fun HourlyCellPreview() {
    KosmosTheme {
        HourlyCell(
            hour = HourlyForecast(14, "2PM", 34, "🌤", isNow = true),
        )
    }
}
