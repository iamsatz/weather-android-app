package com.kosmos.android.ui.travel

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.atoms.KosmosCard
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.engine.PlainLanguage
import com.kosmos.shared.travel.DestinationResult
import com.kosmos.shared.travel.DestinationVibe
import com.kosmos.shared.travel.TravelFilters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TravelScreen(
    contextLine: String,
    filters: TravelFilters,
    results: List<DestinationResult>,
    isLoading: Boolean,
    manualKm: String,
    useCelsius: Boolean,
    onBack: () -> Unit,
    onApplyFilters: (TravelFilters, String) -> Unit,
    onResetFilters: () -> Unit,
    isTabRoot: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val activeChips = remember(filters, manualKm) { buildActiveFilterChips(filters, manualKm) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isTabRoot) "Plan trip" else "Plan a getaway",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = contextLine,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isTabRoot) "Edit plan" else "Back",
                        )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Filters")
                }
                Text(
                    text = if (isLoading) "Searching…" else "${results.size} spots",
                    fontSize = 14.sp,
                    color = KosmosThemeExt.colors.textMuted,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (activeChips.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    activeChips.forEach { chip ->
                        FilterChip(label = chip, selected = true, onClick = { showFilterSheet = true })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = KosmosColor.primary)
                }
            } else if (results.isEmpty()) {
                KosmosCard {
                    Text(
                        text = "No spots match these filters — tap Filters to widen distance or try Beach / Sea.",
                        fontSize = 14.sp,
                        color = KosmosThemeExt.colors.textMuted,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(results, key = { it.destination.id }) { result ->
                        DestinationCard(result = result, useCelsius = useCelsius)
                    }
                    item { Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding)) }
                }
            }
        }
    }

    if (showFilterSheet) {
        TravelFilterSheet(
            initialFilters = filters,
            initialManualKm = manualKm,
            onDismiss = { showFilterSheet = false },
            onApply = { draft, km ->
                onApplyFilters(draft, km)
                showFilterSheet = false
            },
            onReset = {
                onResetFilters()
                showFilterSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TravelFilterSheet(
    initialFilters: TravelFilters,
    initialManualKm: String,
    onDismiss: () -> Unit,
    onApply: (TravelFilters, String) -> Unit,
    onReset: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(initialFilters) { mutableStateOf(initialFilters) }
    var draftKm by remember(initialManualKm) { mutableStateOf(initialManualKm) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text("Filters", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            FilterSection(title = "When are you going?") {
                ChipRowSingleInt(
                    options = TravelFilters.tripWhenOptions.map { (offset, label) -> offset to label },
                    selected = draft.tripDayOffset,
                    onSelect = { offset -> draft = draft.copy(tripDayOffset = offset) },
                )
            }
            FilterSection(title = "How many people?") {
                ChipRowSingle(
                    options = TravelFilters.groupOptions,
                    selectedLabel = draft.groupSize,
                    onSelectLabel = { draft = draft.copy(groupSize = it) },
                )
            }
            FilterSection(title = "Transport") {
                ChipRowSingle(
                    options = TravelFilters.transportOptions,
                    selectedLabel = draft.transport,
                    onSelectLabel = { draft = draft.copy(transport = it) },
                )
            }
            FilterSection(title = "Vibe") {
                ChipRow(
                    options = TravelFilters.vibeOptions,
                    selected = draft.vibes,
                    onToggle = { id ->
                        val current = draft.vibes.toMutableSet()
                        if (id == "any") {
                            current.clear()
                            current.add("any")
                        } else {
                            current.remove("any")
                            if (id in current) {
                                if (current.size > 1) current.remove(id)
                            } else {
                                current.add(id)
                            }
                            if (current.isEmpty()) current.add("any")
                        }
                        draft = draft.copy(vibes = current)
                    },
                )
            }
            FilterSection(title = "Good for") {
                ChipRow(
                    options = TravelFilters.audienceOptions,
                    selected = draft.audiences,
                    onToggle = { id ->
                        val current = draft.audiences.toMutableSet()
                        if (id == "all") {
                            current.clear()
                            current.add("all")
                        } else {
                            current.remove("all")
                            if (id in current) {
                                if (current.size > 1) current.remove(id)
                            } else {
                                current.add(id)
                            }
                            if (current.isEmpty()) current.add("all")
                        }
                        draft = draft.copy(audiences = current)
                    },
                )
            }
            FilterSection(title = "Distance") {
                ChipRowDistance(
                    options = TravelFilters.distancePresets.map { km -> km to "<${km} km" },
                    selected = draft.distanceMaxKm,
                    onSelect = { km ->
                        draft = draft.copy(distanceMaxKm = km)
                        draftKm = ""
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draftKm,
                    onValueChange = {
                        draftKm = it.filter { char -> char.isDigit() }.take(4)
                        draftKm.toIntOrNull()?.let { km ->
                            if (km > 0) draft = draft.copy(distanceMaxKm = km)
                        }
                    },
                    label = { Text("Or enter km") },
                    placeholder = { Text("e.g. 20, 150, 800") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FilterSection(title = "Region") {
                ChipRowSingle(
                    options = TravelFilters.regions.map { it to it },
                    selectedLabel = draft.region,
                    onSelectLabel = { draft = draft.copy(region = it) },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
                Button(
                    onClick = { onApply(draft, draftKm) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

private fun buildActiveFilterChips(filters: TravelFilters, manualKm: String): List<String> {
    val chips = mutableListOf<String>()
    TravelFilters.tripWhenOptions.firstOrNull { it.first == filters.tripDayOffset }?.second?.let { chips += it }
    TravelFilters.groupOptions.firstOrNull { it.first == filters.groupSize }?.second?.let { chips += it }
    TravelFilters.transportOptions.firstOrNull { it.first == filters.transport }?.second?.let { chips += it }
    filters.vibes.filter { it != "any" }.forEach { id ->
        TravelFilters.vibeOptions.firstOrNull { it.first == id }?.second?.let { chips += it }
    }
    filters.audiences.filter { it != "all" }.forEach { id ->
        TravelFilters.audienceOptions.firstOrNull { it.first == id }?.second?.let { chips += it }
    }
    filters.distanceMaxKm?.let { km ->
        chips += if (manualKm.isNotBlank()) "${manualKm} km" else "<$km km"
    }
    if (filters.region != "All India") chips += filters.region
    return chips
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = KosmosThemeExt.colors.textMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (id, label) ->
            FilterChip(label = label, selected = id in selected, onClick = { onToggle(id) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRowSingleInt(
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            FilterChip(label = label, selected = selected == value, onClick = { onSelect(value) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRowDistance(
    options: List<Pair<Int?, String>>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (km, label) ->
            FilterChip(label = label, selected = selected == km, onClick = { onSelect(km) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRowSingle(
    options: List<Pair<String, String>>,
    selectedLabel: String,
    onSelectLabel: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (id, label) ->
            FilterChip(label = label, selected = id == selectedLabel, onClick = { onSelectLabel(id) })
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) KosmosColor.primary else KosmosThemeExt.colors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = if (selected) KosmosColor.primary else KosmosThemeExt.colors.border,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun DestinationCard(
    result: DestinationResult,
    useCelsius: Boolean,
) {
    val dest = result.destination
    val temp = PlainLanguage.toDisplayTemp(result.weather.temp, useCelsius)
    val condition = PlainLanguage.weatherConditionLabel(result.weather.weatherCode)
    val vibeLabel = when (dest.vibe) {
        DestinationVibe.COOL -> "Cooler / Hills"
        DestinationVibe.BEACH -> "Beach / Sea"
        DestinationVibe.WARM -> "Warm"
    }

    KosmosCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dest.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KosmosThemeExt.colors.textPrimary,
                )
                Text(
                    text = "${dest.region} · ${result.distanceKm} km · $vibeLabel",
                    fontSize = 12.sp,
                    color = KosmosThemeExt.colors.textMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$temp°",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KosmosThemeExt.colors.textPrimary,
                )
                Text(
                    text = condition,
                    fontSize = 11.sp,
                    color = KosmosThemeExt.colors.textMuted,
                )
            }
        }
        Text(
            text = result.whyGo ?: result.outlook.summary,
            fontSize = 13.sp,
            color = KosmosThemeExt.colors.textSecondary,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ScorePill("Score ${result.outlook.score}/100")
        }
    }
}

@Composable
private fun ScorePill(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = KosmosColor.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, KosmosColor.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
