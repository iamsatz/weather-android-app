package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.prototype.PrototypeData
import com.kosmos.android.ui.designsystem.atoms.AqiBadge
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.ElderTypography
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled

@Composable
fun HeroBlock(
    snapshot: WeatherSnapshot,
    showNumbers: Boolean = false,
    elderMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotionEnabled()
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 500),
        label = "heroFade",
    )

    val topVerdict = snapshot.verdicts.firstOrNull {
        it.priority == VerdictPriority.SEVERE || it.priority == VerdictPriority.ACTION
    } ?: snapshot.verdicts.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(top = KosmosDimens.grid, bottom = KosmosDimens.sectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = snapshot.locationLine.uppercase(),
            style = KosmosTextStyles.locationHeader,
            color = KosmosColor.textOnGradient.copy(alpha = 0.85f),
        )
        if (snapshot.locationSourceLabel.isNotBlank()) {
            Text(
                text = snapshot.locationSourceLabel,
                style = KosmosTextStyles.dateHeader,
                color = KosmosColor.textOnGradient.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            text = snapshot.dateLabel,
            style = KosmosTextStyles.dateHeader,
            color = KosmosColor.textOnGradient.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        Text(
            text = "${snapshot.temp}°",
            style = if (elderMode) ElderTypography.temp else MaterialTheme.typography.displayLarge,
            color = KosmosColor.textOnGradient,
        )
        topVerdict?.let { verdict ->
            Text(
                text = verdict.title,
                style = KosmosTextStyles.heroVerdictLabel,
                color = KosmosColor.textOnGradient.copy(alpha = 0.92f),
                modifier = Modifier.padding(top = 8.dp),
            )
        } ?: Text(
            text = snapshot.conditionLabel,
            style = KosmosTextStyles.conditionLabel,
            color = KosmosColor.textOnGradient,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.padding(top = KosmosDimens.grid),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Feels like ${snapshot.feelsLike}° · ${snapshot.feelsLikePlain}",
                style = KosmosTextStyles.feelsLike,
                color = KosmosColor.textOnGradient.copy(alpha = 0.9f),
            )
            AqiBadge(
                label = if (showNumbers && snapshot.aqiValue != null) {
                    "${snapshot.aqiLabel} · ${snapshot.aqiValue}"
                } else {
                    snapshot.aqiLabel
                },
                color = Color(snapshot.aqiColor),
            )
        }
        Text(
            text = "H ${snapshot.high}° · L ${snapshot.low}°",
            style = KosmosTextStyles.hiLo,
            color = KosmosColor.textOnGradient.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 4.dp),
        )
        if (showNumbers) {
            Text(
                text = "UV ${snapshot.uvIndex.toInt()} · Humidity ${snapshot.humidity}%",
                style = KosmosTextStyles.hiLo,
                color = KosmosColor.textOnGradient.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF5E89B8)
@Composable
private fun HeroBlockPreview() {
    KosmosTheme {
        HeroBlock(snapshot = PrototypeData.hyderabadSummerDay, showNumbers = true)
    }
}
