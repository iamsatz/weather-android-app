package com.kosmos.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.KosmosIconButton
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles

@Composable
fun HomeSkeleton(
    message: String = "Finding your area…",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5E89B8),
                        Color(0xFF7BA4C9),
                        Color(0xFF9BB8D4),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KosmosDimens.screenHorizontal)
                .padding(top = KosmosDimens.screenTop),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(16.dp)
                                .background(Color.LightGray.copy(alpha = 0.35f), RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(12.dp)
                                .background(Color.LightGray.copy(alpha = 0.25f), RoundedCornerShape(4.dp)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.LightGray.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = KosmosColor.textOnGradient)
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 16.dp),
                    color = KosmosColor.textOnGradient.copy(alpha = 0.92f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Loading weather for you",
                    modifier = Modifier.padding(top = 6.dp),
                    color = KosmosColor.textOnGradient.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

fun WeatherSnapshot.headerLocationLabel(): String =
    locationHeadline.ifBlank { appBarTitle.ifBlank { city } }

fun WeatherSnapshot.headerSubtitle(): String {
    val headline = headerLocationLabel()
    val parts = buildList {
        city.trim()
            .takeIf { it.isNotBlank() && !it.equals(headline, ignoreCase = true) }
            ?.let { add(it) }
        country.trim()
            .takeIf {
                it.isNotBlank() &&
                    !it.equals(headline, ignoreCase = true) &&
                    !it.equals(city, ignoreCase = true)
            }
            ?.let { add(it) }
    }
    return when {
        parts.isNotEmpty() -> parts.joinToString(" · ")
        locationDetail.isNotBlank() -> locationDetail
        else -> ""
    }
}

@Composable
fun HomeLocationHeader(
    locationLabel: String,
    locationSubtitle: String,
    onLocationClick: () -> Unit,
    isLocating: Boolean,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    notificationUnread: Int = 0,
    showSearch: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onLocationClick),
            ) {
                Text(
                    text = "$locationLabel ›",
                    style = KosmosTextStyles.locationHeader,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                when {
                    isLocating -> {
                        Text(
                            text = "Pinpointing your area…",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    locationSubtitle.isNotBlank() -> {
                        Text(
                            text = locationSubtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KosmosIconButton(
                    icon = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    onClick = onNotificationsClick,
                    badgeCount = notificationUnread,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                if (showSearch) {
                    KosmosIconButton(
                        icon = Icons.Default.Search,
                        contentDescription = "Search city",
                        onClick = onSearchClick,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                KosmosIconButton(
                    icon = Icons.Default.Menu,
                    contentDescription = "Settings",
                    onClick = onSettingsClick,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun ElderLocationHeader(
    locationLabel: String,
    locationSubtitle: String,
    onLocationClick: () -> Unit,
    isLocating: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onLocationClick),
        ) {
            Text(
                text = locationLabel,
                style = KosmosTextStyles.locationHeader,
                color = KosmosColor.textOnGradient,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            when {
                isLocating -> {
                    Text(
                        text = "Pinpointing your area…",
                        fontSize = 13.sp,
                        color = KosmosColor.textOnGradient.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                locationSubtitle.isNotBlank() -> {
                    Text(
                        text = locationSubtitle,
                        fontSize = 13.sp,
                        color = KosmosColor.textOnGradient.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        KosmosIconButton(
            icon = Icons.Default.Menu,
            contentDescription = "Settings",
            onClick = onSettingsClick,
        )
    }
}
