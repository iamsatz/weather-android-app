package com.kosmos.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.GeocodeSearchResult
import com.kosmos.android.ui.designsystem.tokens.KosmosColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchSheet(
    searchResults: List<GeocodeSearchResult>,
    currentLocationLine: String,
    hasLocationPermission: Boolean,
    isLocating: Boolean,
    onQueryChange: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onPlaceSelected: (GeocodeSearchResult) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text(
                text = "Change location",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLocating) {
                        onUseCurrentLocation()
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = KosmosColor.primary,
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = "Use current location",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = if (hasLocationPermission) {
                            "Uses your phone location"
                        } else {
                            "Tap to allow location access"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

            Text(
                text = "Currently: $currentLocationLine",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onQueryChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search city or area…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            onQueryChange("")
                        }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                singleLine = true,
            )

            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(searchResults, key = { "${it.latitude},${it.longitude}" }) { result ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaceSelected(result) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(result.label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        if (result.detail.isNotBlank()) {
                            Text(
                                result.detail,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }
            }
        }
    }
}
