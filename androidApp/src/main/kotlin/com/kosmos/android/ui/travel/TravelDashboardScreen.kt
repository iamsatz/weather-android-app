package com.kosmos.android.ui.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.HomeWeatherSummary
import com.kosmos.android.data.TravelDashboardData
import com.kosmos.android.data.TravelState
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.molecules.VerdictRow
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.travel.PackItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDashboardScreen(
    snapshot: WeatherSnapshot,
    travelState: TravelState,
    dashboard: TravelDashboardData?,
    travelVerdicts: List<Verdict>,
    isLoading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Travel dashboard", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${travelState.distanceKm} km from home",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (isLoading || dashboard == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = KosmosColor.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KosmosDimens.screenHorizontal),
        ) {
            item {
                WeatherSplitCard(
                    currentLine = snapshot.locationLine,
                    currentTemp = snapshot.temp,
                    currentCondition = snapshot.conditionLabel,
                    home = dashboard.home,
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionTitle("PACK LIST")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(dashboard.packItems) { item ->
                PackItemRow(item = item)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionTitle("TRIP VERDICTS")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(travelVerdicts) { verdict ->
                VerdictRow(
                    verdict = verdict,
                    expanded = true,
                    onClick = {},
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            item { Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding)) }
        }
    }
}

@Composable
private fun WeatherSplitCard(
    currentLine: String,
    currentTemp: Int,
    currentCondition: String,
    home: HomeWeatherSummary,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, KosmosThemeExt.colors.border, RoundedCornerShape(14.dp))
            .background(KosmosColor.primary.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SplitRow(emoji = "📍", label = currentLine, temp = currentTemp, condition = currentCondition)
        SplitRow(emoji = "🏠", label = home.locationLine, temp = home.temp, condition = home.conditionLabel)
    }
}

@Composable
private fun SplitRow(emoji: String, label: String, temp: Int, condition: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KosmosThemeExt.colors.textPrimary,
                )
                Text(
                    text = condition,
                    fontSize = 12.sp,
                    color = KosmosThemeExt.colors.textMuted,
                )
            }
        }
        Text(
            text = "${temp}°",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KosmosColor.primary,
        )
    }
}

@Composable
private fun PackItemRow(item: PackItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, KosmosThemeExt.colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = item.emoji, fontSize = 20.sp)
        Column {
            Text(
                text = item.item,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = KosmosThemeExt.colors.textPrimary,
            )
            Text(
                text = item.reason,
                fontSize = 12.sp,
                color = KosmosThemeExt.colors.textSecondary,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = KosmosThemeExt.colors.textMuted,
        letterSpacing = 1.sp,
    )
}
