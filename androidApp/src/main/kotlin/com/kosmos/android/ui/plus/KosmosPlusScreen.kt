package com.kosmos.android.ui.plus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.i18n.localized
import com.kosmos.android.ui.designsystem.atoms.KosmosCard
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KosmosPlusScreen(
    isPlus: Boolean,
    onSubscribe: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(localized("kosmos_plus"), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (isPlus) "You're on Kosmos+" else "Unlock the full Kosmos experience",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KosmosThemeExt.colors.textPrimary,
            )

            PlanFeature("✨", localized("plus_unlimited_chat"))
            PlanFeature("🎭", localized("plus_all_modes"))
            PlanFeature("🔊", localized("plus_voice"))
            PlanFeature("📍", "Multi-location (coming soon)")

            Spacer(modifier = Modifier.height(8.dp))

            if (isPlus) {
                KosmosCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Active subscription",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Button(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(localized("subscribe"))
                }
                OutlinedButton(
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(localized("restore"))
                }
            }
        }
    }
}

@Composable
private fun PlanFeature(emoji: String, text: String) {
    Text(
        text = "$emoji  $text",
        fontSize = 16.sp,
        color = KosmosThemeExt.colors.textPrimary,
    )
}
