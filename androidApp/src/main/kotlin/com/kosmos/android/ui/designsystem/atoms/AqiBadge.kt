package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun AqiBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(KosmosShape.chip)
            .background(KosmosThemeExt.colors.cardBackground)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "●", color = color, fontSize = 10.sp)
        Text(
            text = label,
            color = KosmosThemeExt.colors.textPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Preview
@Composable
private fun AqiBadgePreview() {
    KosmosTheme {
        AqiBadge(label = "Good", color = Color(0xFF4DC85A))
    }
}
