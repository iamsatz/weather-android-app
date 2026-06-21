package com.kosmos.android.ui.modes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.i18n.S
import com.kosmos.android.ui.designsystem.molecules.ModeCardData
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeDetailSheet(
    mode: ModeCardData,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = mode.emoji, fontSize = 40.sp)
            Text(
                text = mode.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KosmosThemeExt.colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = mode.description,
                fontSize = 14.sp,
                color = KosmosThemeExt.colors.textMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            if (mode.isComingSoon) {
                Text(
                    text = "🔒 ${S.get("coming_soon")} · ${S.get("coming_soon_beta")}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (mode.plannedInsights.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = S.get("whats_included"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = KosmosThemeExt.colors.textMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
                mode.plannedInsights.forEach { insight ->
                    Text(
                        text = if (mode.isComingSoon) "○ $insight" else "✓ $insight",
                        fontSize = 14.sp,
                        color = KosmosThemeExt.colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
