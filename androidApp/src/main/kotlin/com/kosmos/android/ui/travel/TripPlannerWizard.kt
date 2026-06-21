package com.kosmos.android.ui.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.GeocodeSearchResult
import com.kosmos.android.data.TripWizardDraft
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.travel.TravelFilters

private const val TOTAL_STEPS = 6

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripPlannerWizard(
    initialFilters: TravelFilters,
    savedDraft: TripWizardDraft?,
    searchResults: List<GeocodeSearchResult>,
    onSearchQueryChange: (String) -> Unit,
    onDraftChange: (TripWizardDraft) -> Unit,
    onComplete: (TravelFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableIntStateOf(savedDraft?.step ?: 0) }
    var destinationQuery by rememberSaveable { mutableStateOf(savedDraft?.destinationQuery ?: initialFilters.destinationQuery) }
    var destinationLat by rememberSaveable { mutableStateOf(savedDraft?.destinationLat ?: initialFilters.destinationLat) }
    var destinationLon by rememberSaveable { mutableStateOf(savedDraft?.destinationLon ?: initialFilters.destinationLon) }
    var destinationLabel by rememberSaveable { mutableStateOf(savedDraft?.destinationLabel ?: initialFilters.destinationLabel) }
    var pickedPlaceConfirmed by rememberSaveable { mutableStateOf(savedDraft?.pickedPlaceConfirmed ?: false) }
    var distanceMaxKm by rememberSaveable {
        mutableIntStateOf(
            savedDraft?.distanceMaxKm
                ?: initialFilters.distanceMaxKm?.takeIf { it != Int.MAX_VALUE }
                ?: 100,
        )
    }
    var selectedVibe by rememberSaveable { mutableStateOf(savedDraft?.selectedVibe ?: initialFilters.vibes.firstOrNull { it != "any" } ?: "any") }
    var tripDayOffset by rememberSaveable { mutableIntStateOf(savedDraft?.tripDayOffset ?: initialFilters.tripDayOffset) }
    var tripDays by rememberSaveable { mutableIntStateOf(savedDraft?.tripDays ?: initialFilters.tripDays) }
    var audience by rememberSaveable { mutableStateOf(savedDraft?.audience ?: initialFilters.audiences.firstOrNull { it != "all" } ?: "solo") }
    var tripPurposesRaw by rememberSaveable {
        mutableStateOf((savedDraft?.tripPurposes ?: initialFilters.tripPurposes).joinToString("|"))
    }
    val tripPurposes = tripPurposesRaw.split("|").filter { it.isNotBlank() }.toSet().ifEmpty { setOf("any") }

    fun persistDraft() {
        onDraftChange(
            TripWizardDraft(
                step = step,
                destinationQuery = destinationQuery,
                destinationLat = destinationLat,
                destinationLon = destinationLon,
                destinationLabel = destinationLabel,
                pickedPlaceConfirmed = pickedPlaceConfirmed,
                distanceMaxKm = distanceMaxKm,
                selectedVibe = selectedVibe,
                tripDayOffset = tripDayOffset,
                tripDays = tripDays,
                audience = audience,
                tripPurposes = tripPurposes,
            ),
        )
    }

    LaunchedEffect(destinationQuery) {
        if (destinationQuery.length >= 2 && !pickedPlaceConfirmed) {
            onSearchQueryChange(destinationQuery)
        }
    }

    fun clearPickedPlace() {
        destinationLat = null
        destinationLon = null
        destinationLabel = ""
        pickedPlaceConfirmed = false
    }

    fun buildFilters(): TravelFilters {
        val vibeSet = when (selectedVibe) {
            "any", "near" -> setOf("any")
            else -> setOf(selectedVibe)
        }
        val audienceSet = when (audience) {
            "solo" -> setOf("all")
            else -> setOf(audience)
        }
        return initialFilters.copy(
            destinationQuery = destinationQuery.trim(),
            destinationLat = destinationLat,
            destinationLon = destinationLon,
            destinationLabel = destinationLabel.ifBlank { destinationQuery.trim() },
            distanceMaxKm = if (distanceMaxKm <= 0 || distanceMaxKm == Int.MAX_VALUE) Int.MAX_VALUE else distanceMaxKm,
            vibes = vibeSet,
            tripDayOffset = tripDayOffset,
            tripDays = tripDays,
            audiences = audienceSet,
            tripPurposes = tripPurposes.ifEmpty { setOf("any") },
            groupSize = when (audience) {
                "solo" -> "solo"
                "couples" -> "2"
                "family" -> "3-5"
                else -> "3-5"
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KosmosDimens.screenHorizontal),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Plan trip",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = KosmosThemeExt.colors.textPrimary,
            )
            WizardProgressDots(step = step, total = TOTAL_STEPS)
            Spacer(Modifier.height(24.dp))

            when (step) {
                0 -> {
                    WizardQuestion("Where do you want to go?")
                    OutlinedTextField(
                        value = destinationQuery,
                        onValueChange = { newValue ->
                            if (pickedPlaceConfirmed && newValue != destinationQuery) {
                                clearPickedPlace()
                            }
                            destinationQuery = newValue
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search a city or area…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                    if (searchResults.isNotEmpty() && destinationQuery.length >= 2 && !pickedPlaceConfirmed) {
                        Spacer(Modifier.height(8.dp))
                        searchResults.take(5).forEach { result ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        destinationQuery = result.label
                                        destinationLat = result.latitude
                                        destinationLon = result.longitude
                                        destinationLabel = result.label
                                        pickedPlaceConfirmed = true
                                        persistDraft()
                                    }
                                    .padding(vertical = 10.dp),
                            ) {
                                Text(result.label, fontWeight = FontWeight.Medium)
                                Text(
                                    result.detail,
                                    fontSize = 12.sp,
                                    color = KosmosThemeExt.colors.textMuted,
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TravelFilters.wizardDestinationChips.forEach { (label, vibeId) ->
                            WizardChip(
                                label = label,
                                selected = selectedVibe == vibeId,
                                onClick = {
                                    selectedVibe = vibeId
                                    destinationQuery = label
                                    clearPickedPlace()
                                    persistDraft()
                                },
                            )
                        }
                    }
                }
                1 -> {
                    WizardQuestion("How far are you willing to go?")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TravelFilters.wizardDistanceOptions.forEach { km ->
                            val label = when (km) {
                                null -> "Any distance"
                                else -> "${km} km"
                            }
                            WizardChip(
                                label = label,
                                selected = if (km == null) {
                                    distanceMaxKm <= 0 || distanceMaxKm == Int.MAX_VALUE
                                } else {
                                    distanceMaxKm == km
                                },
                                onClick = {
                                    distanceMaxKm = km ?: Int.MAX_VALUE
                                    persistDraft()
                                },
                            )
                        }
                    }
                }
                2 -> {
                    WizardQuestion("When are you planning?")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TravelFilters.tripWhenOptions.forEach { (offset, label) ->
                            WizardChip(
                                label = label,
                                selected = tripDayOffset == offset,
                                onClick = {
                                    tripDayOffset = offset
                                    persistDraft()
                                },
                            )
                        }
                    }
                }
                3 -> {
                    WizardQuestion("How many days?")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TravelFilters.tripDaysOptions.forEach { days ->
                            WizardChip(
                                label = if (days >= 7) "1 week+" else "$days day${if (days > 1) "s" else ""}",
                                selected = tripDays == days,
                                onClick = {
                                    tripDays = days
                                    persistDraft()
                                },
                            )
                        }
                    }
                }
                4 -> {
                    WizardQuestion("Who is going?")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TravelFilters.wizardAudienceOptions.forEach { (id, label) ->
                            WizardChip(
                                label = label,
                                selected = audience == id,
                                onClick = {
                                    audience = id
                                    persistDraft()
                                },
                            )
                        }
                    }
                }
                5 -> {
                    WizardQuestion("What kind of trip? (pick one or more)")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TravelFilters.tripPurposeOptions.forEach { (id, label) ->
                            WizardChip(
                                label = label,
                                selected = id in tripPurposes,
                                onClick = {
                                    tripPurposesRaw = if (id in tripPurposes) {
                                        (tripPurposes - id).ifEmpty { setOf("any") }.joinToString("|")
                                    } else if (id == "any") {
                                        "any"
                                    } else {
                                        (tripPurposes - "any" + id).joinToString("|")
                                    }
                                    persistDraft()
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (step > 0) {
                    TextButton(onClick = {
                        step -= 1
                        persistDraft()
                    }) {
                        Text("Back")
                    }
                } else {
                    Spacer(Modifier.size(1.dp))
                }
                if (step < TOTAL_STEPS - 1) {
                    Button(onClick = {
                        step += 1
                        persistDraft()
                    }) {
                        Text("Next")
                    }
                } else {
                    Button(onClick = { onComplete(buildFilters()) }) {
                        Text("See weather picks")
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardQuestion(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = KosmosThemeExt.colors.textPrimary,
        modifier = Modifier.padding(bottom = 16.dp),
    )
}

@Composable
private fun WizardProgressDots(step: Int, total: Int) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == step) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index <= step) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            KosmosThemeExt.colors.textMuted.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
        Text(
            text = "${step + 1}/$total",
            fontSize = 12.sp,
            color = KosmosThemeExt.colors.textMuted,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun WizardChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = KosmosThemeExt.colors
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else colors.cardBackground,
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else colors.textMuted.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else colors.textPrimary,
    )
}

@Composable
fun TripPlanTab(
    filters: TravelFilters,
    results: List<com.kosmos.shared.travel.DestinationResult>,
    contextLine: String,
    isLoading: Boolean,
    manualKm: String,
    useCelsius: Boolean,
    showResults: Boolean,
    savedDraft: TripWizardDraft?,
    searchResults: List<GeocodeSearchResult>,
    onSearchQueryChange: (String) -> Unit,
    onDraftChange: (TripWizardDraft) -> Unit,
    onWizardComplete: (TravelFilters) -> Unit,
    onEditPlan: () -> Unit,
    onApplyFilters: (TravelFilters, String) -> Unit,
    onResetFilters: () -> Unit,
) {
    if (!showResults) {
        TripPlannerWizard(
            initialFilters = filters,
            savedDraft = savedDraft,
            searchResults = searchResults,
            onSearchQueryChange = onSearchQueryChange,
            onDraftChange = onDraftChange,
            onComplete = onWizardComplete,
        )
    } else {
        TravelScreen(
            contextLine = contextLine,
            filters = filters,
            results = results,
            isLoading = isLoading,
            manualKm = manualKm,
            useCelsius = useCelsius,
            onBack = onEditPlan,
            onApplyFilters = onApplyFilters,
            onResetFilters = onResetFilters,
            isTabRoot = true,
        )
    }
}
