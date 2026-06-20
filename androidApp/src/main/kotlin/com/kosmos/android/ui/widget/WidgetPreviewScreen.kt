package com.kosmos.android.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.AqiBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPreviewScreen(
    snapshot: WeatherSnapshot,
    onBack: () -> Unit,
) {
    val topVerdicts = snapshot.verdicts
        .filter { it.priority != VerdictPriority.NORMAL }
        .take(2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Preview", fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp),
        ) {
            Text(
                text = "Medium widget (2×1) — the product for Android users",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            MediumWidgetPreview(snapshot = snapshot, verdicts = topVerdicts)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tap widget on home screen → opens app. Verdict deep-links land in v1.1.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun MediumWidgetPreview(
    snapshot: WeatherSnapshot,
    verdicts: List<com.kosmos.android.model.Verdict>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF5E89B8))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${snapshot.locationLine} · ${snapshot.temp}°",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🌤 ${snapshot.conditionLabel}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                AqiBadge(
                    label = snapshot.aqiLabel,
                    color = Color(snapshot.aqiColor),
                )
            }
        }

        if (verdicts.size == 1) {
            Text(
                text = "${verdicts[0].emoji} ${verdicts[0].title}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                verdicts.forEach { verdict ->
                    Text(
                        text = "${verdict.emoji} ${verdict.title}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
