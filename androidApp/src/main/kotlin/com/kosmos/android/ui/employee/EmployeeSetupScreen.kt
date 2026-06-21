package com.kosmos.android.ui.employee

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.i18n.localized
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import com.kosmos.shared.mode.EmployeeProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSetupScreen(
    profile: EmployeeProfile,
    currentLocationLine: String,
    onBack: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onSave: (EmployeeProfile) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var workQuery by rememberSaveable { mutableStateOf(profile.workLabel ?: "") }
    val homeLine = profile.homeLabel ?: currentLocationLine

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(localized("employee_setup_title")) },
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
                text = localized("employee_setup_sub"),
                color = KosmosThemeExt.colors.textSecondary,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            Text(
                text = localized("employee_home"),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = KosmosThemeExt.colors.textMuted,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = homeLine, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onUseCurrentLocation, modifier = Modifier.fillMaxWidth()) {
                Text(localized("employee_use_current"))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = localized("employee_work"),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = KosmosThemeExt.colors.textMuted,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = workQuery,
                onValueChange = { workQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(localized("employee_work_hint")) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onSave(
                        profile.copy(workLabel = workQuery.trim().takeIf { it.isNotBlank() }),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = workQuery.isNotBlank(),
            ) {
                Text(localized("employee_save"))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(localized("employee_skip"))
            }
        }
    }
}
