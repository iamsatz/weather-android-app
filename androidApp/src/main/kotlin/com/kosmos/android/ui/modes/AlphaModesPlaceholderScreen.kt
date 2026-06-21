package com.kosmos.android.ui.modes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun AlphaModesPlaceholderScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = "More coming soon",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = KosmosThemeExt.colors.textPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Kosmos Alpha is built for your working day — commute, heat, rain, air. " +
                    "Optional extras like Photographer mode land in Beta 1 after we lock the information.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = KosmosThemeExt.colors.textSecondary,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Beta 1: Photographer\nBeta 2: full design polish\nLater: Farmer, Family, Elder, and more",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = KosmosThemeExt.colors.textMuted,
            )
        }
    }
}
