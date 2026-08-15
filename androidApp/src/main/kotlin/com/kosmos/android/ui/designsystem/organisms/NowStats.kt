package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles

@Composable
fun NowStats(
    snapshot: WeatherSnapshot,
    modifier: Modifier = Modifier,
) {
    val wind = snapshot.windSpeedKmh.toInt()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${snapshot.temp}°",
                style = KosmosTextStyles.temp,
                color = KosmosColor.temp,
            )
            Column(horizontalAlignment = Alignment.Start) {
                StatLine("Wind ", "$wind km/h")
                StatLine("Humidity ", "${snapshot.humidity}%")
                Text(
                    text = "H ${snapshot.high}°   L ${snapshot.low}°",
                    style = KosmosTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                    color = KosmosColor.textPrimary,
                    modifier = Modifier.padding(top = KosmosDimens.sm),
                )
            }
        }
        StatLine(
            "Feels Like ",
            "${snapshot.feelsLike}°",
            modifier = Modifier.padding(top = KosmosDimens.sm),
        )
    }
}

@Composable
private fun StatLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            append(label)
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value) }
        },
        style = KosmosTextStyles.caption,
        color = KosmosColor.textPrimary,
        modifier = modifier,
    )
}
