package com.kosmos.android.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.WeatherSnapshot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    snapshot: WeatherSnapshot,
    showTenDay: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timedVerdicts = snapshot.verdicts.filter { it.timeWindow != null }
    val hours = (5..23).toList()
    var uvExpanded by remember { mutableStateOf(false) }
    var forecastExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Day Timeline", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${snapshot.locationLine} · ${snapshot.dateLabel}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            item {
                Text(
                    text = "Verdicts placed at their time windows — scroll to see your whole day.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            if (showTenDay && snapshot.hourly.any { it.uvPlain != null }) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uvExpanded = !uvExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "HOURLY UV · KOSMOS+",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                        )
                        Icon(
                            if (uvExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (uvExpanded) "Collapse" else "Expand",
                        )
                    }
                }
                if (uvExpanded) {
                    items(snapshot.hourly.take(12)) { hour ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(hour.label, fontSize = 13.sp)
                            Text(hour.uvPlain ?: "—", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            if (showTenDay && snapshot.daily.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { forecastExpanded = !forecastExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "10-DAY FORECAST · KOSMOS+",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                        )
                        Icon(
                            if (forecastExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (forecastExpanded) "Collapse" else "Expand",
                        )
                    }
                }
                if (forecastExpanded) {
                    items(snapshot.daily) { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(day.dateLabel, fontSize = 14.sp)
                            Text("${day.high}° / ${day.low}°", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            if (day.precipMax >= 30) {
                                Text("💧${day.precipMax}%", fontSize = 12.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            items(hours) { hour ->
                TimelineHourRow(
                    hour = hour,
                    verdicts = timedVerdicts.filter { it.timeWindow?.contains(hour) == true },
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TimelineHourRow(
    hour: Int,
    verdicts: List<Verdict>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = formatHour(hour),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(KosmosDimens.timelineHourWidth),
        )

        Box(
            modifier = Modifier
                .width(2.dp)
                .height(if (verdicts.isEmpty()) 20.dp else (verdicts.size * 44).dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            if (verdicts.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                verdicts.forEach { verdict ->
                    VerdictChip(verdict = verdict)
                }
            }
        }
    }
}

@Composable
private fun VerdictChip(verdict: Verdict) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KosmosShape.timelineChip)
            .background(Color(verdict.accentColor).copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = verdict.emoji, fontSize = 14.sp)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = verdict.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            verdict.timeWindow?.let {
                Text(
                    text = it.label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

private fun formatHour(hour: Int): String = when {
    hour == 0 -> "12 AM"
    hour < 12 -> "$hour AM"
    hour == 12 -> "12 PM"
    else -> "${hour - 12} PM"
}
