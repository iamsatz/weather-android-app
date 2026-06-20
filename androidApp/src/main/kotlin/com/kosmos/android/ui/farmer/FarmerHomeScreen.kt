package com.kosmos.android.ui.farmer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.DailyForecast
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.SectionLabel
import com.kosmos.android.ui.designsystem.molecules.VerdictRow
import com.kosmos.android.ui.designsystem.organisms.HeroBlock
import com.kosmos.android.ui.designsystem.organisms.WeatherBackground
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.engine.PlainLanguage

@Composable
fun FarmerHomeScreen(
    snapshot: WeatherSnapshot,
    daily: List<DailyForecast>,
    showNumbers: Boolean,
    plotLabel: String?,
    onSettingsClick: () -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val verdicts = snapshot.verdicts.filter { it.priority != VerdictPriority.NORMAL }

    WeatherBackground(snapshot = snapshot) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KosmosDimens.screenHorizontal),
        ) {
            Spacer(modifier = Modifier.height(KosmosDimens.screenTop))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = plotLabel ?: snapshot.locationLine,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KosmosColor.textOnGradient,
                    )
                    Text(
                        text = "🌾 Farmer mode",
                        fontSize = 13.sp,
                        color = KosmosColor.textOnGradient.copy(alpha = 0.8f),
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = KosmosColor.textOnGradient)
                }
            }

            HeroBlock(snapshot = snapshot, showNumbers = showNumbers, elderMode = true)

            Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
            SectionLabel("7-DAY OUTLOOK")
            SevenDayStrip(daily = daily.take(7), useCelsius = true)

            Spacer(modifier = Modifier.height(KosmosDimens.sectionSpacing))
            SectionLabel("CROP INSIGHTS")
            verdicts.forEach { verdict ->
                VerdictRow(
                    verdict = verdict,
                    expanded = true,
                    onClick = {},
                    elderMode = true,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🔊 Read aloud",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = KosmosColor.primary,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .clickable(onClick = onReadAloud),
            )

            Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding))
        }
    }
}

@Composable
private fun SevenDayStrip(daily: List<DailyForecast>, useCelsius: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        daily.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.dateLabel, fontSize = 11.sp, color = KosmosThemeExt.colors.textMuted)
                Text(
                    "${day.high}°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = KosmosThemeExt.colors.textPrimary,
                )
                Text(
                    "${day.low}°",
                    fontSize = 11.sp,
                    color = KosmosThemeExt.colors.textMuted,
                )
            }
        }
    }
}
