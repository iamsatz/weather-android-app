package com.kosmos.android.ui.travel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.HyperLocalResult
import com.kosmos.android.data.SavedPlace
import com.kosmos.android.ui.designsystem.atoms.KosmosCard
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperLocalScreen(
    result: HyperLocalResult?,
    isLoading: Boolean,
    currentLocationLabel: String,
    savedPlaces: List<SavedPlace>,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onSelectPlace: (SavedPlace) -> Unit,
    onRemovePlace: (SavedPlace) -> Unit,
    onAddPlaceFromSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hyper-local", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = result?.areaName ?: currentLocationLabel,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KosmosDimens.screenHorizontal),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Detail") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Places") })
            }

            when (selectedTab) {
                0 -> HyperLocalDetailTab(
                    query = query,
                    onQueryChange = { query = it },
                    isLoading = isLoading,
                    result = result,
                    onSearch = onSearch,
                    onUseCurrentLocation = onUseCurrentLocation,
                    onSaveSearch = { if (query.isNotBlank()) onAddPlaceFromSearch(query.trim()) },
                )
                1 -> HyperLocalPlacesTab(
                    currentLocationLabel = currentLocationLabel,
                    savedPlaces = savedPlaces,
                    onUseCurrentLocation = onUseCurrentLocation,
                    onSelectPlace = onSelectPlace,
                    onRemovePlace = onRemovePlace,
                )
            }
        }
    }
}

@Composable
private fun HyperLocalDetailTab(
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    result: HyperLocalResult?,
    onSearch: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onSaveSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search area") },
            placeholder = { Text("Bangalore, Gachibowli, Biranguda…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { if (query.isNotBlank()) onSearch(query.trim()) },
            enabled = query.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Loading…" else "Check this area")
        }
        Text(
            text = "Use current location",
            color = KosmosColor.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable(onClick = onUseCurrentLocation),
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = KosmosColor.primary)
                }
            }
            result != null -> {
                Text(
                    text = "${result.temp}° · ${result.condition}",
                    style = KosmosTextStyles.heroVerdictLabel,
                    color = KosmosColor.primary,
                )
                result.cards.forEach { card ->
                    Spacer(modifier = Modifier.height(KosmosDimens.cardGap))
                    KosmosCard {
                        Text(
                            text = card.title.uppercase(),
                            style = KosmosTextStyles.locationHeader,
                            color = KosmosThemeExt.colors.textMuted,
                        )
                        Text(
                            text = card.body,
                            style = KosmosTextStyles.verdictDetail,
                            color = KosmosThemeExt.colors.textPrimary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                if (result.fusionSources.isNotEmpty()) {
                    Text(
                        text = "Sources: ${result.fusionSources.joinToString(" + ")}",
                        fontSize = 11.sp,
                        color = KosmosThemeExt.colors.textMuted,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                if (query.isNotBlank()) {
                    Text(
                        text = "Save to Places",
                        color = KosmosColor.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clickable(onClick = onSaveSearch),
                    )
                }
            }
            else -> {
                Text(
                    text = "Your current area loads automatically. Search to check another neighborhood — same fused data as Home.",
                    style = KosmosTextStyles.settingsSubtitle,
                    color = KosmosThemeExt.colors.textMuted,
                )
            }
        }
        Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding))
    }
}

@Composable
private fun HyperLocalPlacesTab(
    currentLocationLabel: String,
    savedPlaces: List<SavedPlace>,
    onUseCurrentLocation: () -> Unit,
    onSelectPlace: (SavedPlace) -> Unit,
    onRemovePlace: (SavedPlace) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(KosmosDimens.cardGap),
    ) {
        KosmosCard(onClick = onUseCurrentLocation) {
            RowWithIcon(
                icon = { Icon(Icons.Default.MyLocation, null, tint = KosmosColor.primary) },
                title = "Current location",
                subtitle = currentLocationLabel,
            )
        }
        savedPlaces.forEach { place ->
            KosmosCard(onClick = { onSelectPlace(place) }) {
                RowWithIcon(
                    title = place.label,
                    subtitle = place.city,
                    trailing = {
                        Text(
                            text = "Remove",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onRemovePlace(place) },
                        )
                    },
                )
            }
        }
        if (savedPlaces.isEmpty()) {
            Text(
                text = "Search an area on the Detail tab, then tap Save to Places.",
                style = KosmosTextStyles.settingsSubtitle,
                color = KosmosThemeExt.colors.textMuted,
            )
        }
        Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding))
    }
}

@Composable
private fun RowWithIcon(
    title: String,
    subtitle: String,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.invoke()
        Column(modifier = Modifier.weight(1f).padding(start = if (icon != null) 12.dp else 0.dp)) {
            Text(text = title, style = KosmosTextStyles.settingsTitle)
            Text(text = subtitle, style = KosmosTextStyles.settingsSubtitle, modifier = Modifier.padding(top = 2.dp))
        }
        trailing?.invoke()
    }
}
