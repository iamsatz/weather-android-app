package com.kosmos.android.ui.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val colors = KosmosThemeExt.colors

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = KosmosTextStyles.settingsTitle, color = colors.textPrimary)
                Text(text = subtitle, style = KosmosTextStyles.settingsSubtitle, color = colors.textMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.semantics {
                    contentDescription = "$title, ${if (checked) "on" else "off"}"
                },
            )
        }
        if (showDivider) {
            HorizontalDivider(color = colors.border)
        }
    }
}

@Composable
fun SettingsLinkRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val colors = KosmosThemeExt.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(text = title, style = KosmosTextStyles.settingsTitle, color = colors.textPrimary)
            Text(text = subtitle, style = KosmosTextStyles.settingsSubtitle, color = colors.textMuted)
        }
        if (showDivider) {
            HorizontalDivider(color = colors.border)
        }
    }
}

@Preview
@Composable
private fun SettingsRowPreview() {
    KosmosTheme {
        SettingsRow(
            title = "Temperature",
            subtitle = "Celsius °C",
            checked = true,
            onToggle = {},
        )
    }
}
