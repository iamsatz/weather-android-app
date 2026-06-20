package com.kosmos.android.ui.farmer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.farmer.CropType
import com.kosmos.shared.farmer.FarmerProfile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FarmerSetupScreen(
    profile: FarmerProfile,
    onBack: () -> Unit,
    onSave: (FarmerProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCrops by rememberSaveable { mutableStateOf(profile.crops) }
    var plotCity by rememberSaveable { mutableStateOf(profile.plotCity ?: "") }
    var sowingDate by rememberSaveable { mutableStateOf(profile.sowingDateIso ?: "") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Farmer setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KosmosDimens.screenHorizontal)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "CROPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = KosmosThemeExt.colors.textMuted,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CropType.entries.forEach { crop ->
                    val on = crop.id in selectedCrops
                    Text(
                        text = crop.label,
                        fontSize = 13.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (on) KosmosColor.primary else KosmosThemeExt.colors.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, if (on) KosmosColor.primary else KosmosThemeExt.colors.border, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedCrops = if (on) selectedCrops - crop.id else selectedCrops + crop.id
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = plotCity,
                onValueChange = { plotCity = it },
                label = { Text("Plot location (city/village)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = sowingDate,
                onValueChange = { sowingDate = it },
                label = { Text("Sowing date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            crops = selectedCrops,
                            plotCity = plotCity.ifBlank { null },
                            sowingDateIso = sowingDate.ifBlank { null },
                        ),
                    )
                },
                enabled = selectedCrops.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save farmer profile")
            }
            Spacer(modifier = Modifier.height(KosmosDimens.bottomScrollPadding))
        }
    }
}
