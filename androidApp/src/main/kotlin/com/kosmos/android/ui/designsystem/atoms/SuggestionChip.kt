package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .clip(KosmosShape.chip)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Preview
@Composable
private fun SuggestionChipPreview() {
    KosmosTheme {
        SuggestionChip(text = "Should I run at 6 PM?", onClick = {})
    }
}
