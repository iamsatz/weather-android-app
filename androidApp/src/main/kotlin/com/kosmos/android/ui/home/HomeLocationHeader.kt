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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.kosmos.android.R
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.ui.designsystem.atoms.KosmosIconButton
import com.kosmos.android.ui.designsystem.atoms.LocationPill
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun HomeSkeleton(
    message: String = "Finding your area…",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KosmosThemeExt.colors.bgSubtle),
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
                CircularProgressIndicator(color = KosmosColor.accent)
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 16.dp),
                    color = KosmosThemeExt.colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Loading weather for you",
                    modifier = Modifier.padding(top = 6.dp),
                    color = KosmosThemeExt.colors.textSecondary,
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
    showChrome: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LocationPill(
            city = locationLabel,
            onClick = onLocationClick,
            locating = isLocating,
            modifier = if (showChrome) Modifier.weight(1f) else Modifier,
        )
        if (showChrome) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KosmosIconButton(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = "Notifications",
                    onClick = onNotificationsClick,
                    badgeCount = notificationUnread,
                    tint = KosmosThemeExt.colors.textPrimary,
                )
                if (showSearch) {
                    KosmosIconButton(
                        painter = painterResource(R.drawable.ic_magnifying_glass),
                        contentDescription = "Search city",
                        onClick = onSearchClick,
                        tint = KosmosThemeExt.colors.textPrimary,
                    )
                }
                KosmosIconButton(
                    painter = painterResource(R.drawable.ic_gear),
                    contentDescription = "Settings",
                    onClick = onSettingsClick,
                    tint = KosmosThemeExt.colors.textPrimary,
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
            painter = painterResource(R.drawable.ic_gear),
            contentDescription = "Settings",
            onClick = onSettingsClick,
        )
    }
}
