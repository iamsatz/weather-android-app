package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

@Composable
fun KosmosFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = Color.White,
        contentColor = KosmosColor.primary,
        shape = KosmosShape.fab,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Preview
@Composable
private fun KosmosFabPreview() {
    KosmosTheme {
        KosmosFab(label = "Ask Kosmos", onClick = {})
    }
}
