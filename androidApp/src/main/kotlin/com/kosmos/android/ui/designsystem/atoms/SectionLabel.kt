package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = KosmosColor.textOnGradient.copy(alpha = 0.85f),
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier.padding(bottom = 10.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF5E89B8)
@Composable
private fun SectionLabelPreview() {
    KosmosTheme {
        SectionLabel("INSIGHTS")
    }
}
