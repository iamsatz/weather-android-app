package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kosmos.android.R
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt

@Composable
fun LocationPill(
    city: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    locating: Boolean = false,
) {
    val colors = KosmosThemeExt.colors
    Row(
        modifier = modifier
            .clip(KosmosShape.pill)
            .background(colors.bgPill)
            .clickable(onClick = onClick)
            .heightIn(min = KosmosDimens.touch)
            .widthIn(max = 303.dp)
            .padding(horizontal = KosmosDimens.md, vertical = KosmosDimens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_map_pin),
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(KosmosDimens.iconChrome),
        )
        Text(
            text = if (locating) "Pinpointing…" else city,
            style = KosmosTextStyles.cityPill,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = KosmosDimens.sm, end = 4.dp)
                .weight(1f, fill = false),
        )
        Icon(
            painter = painterResource(R.drawable.ic_caret_down),
            contentDescription = "Change city — currently $city",
            tint = colors.textSecondary,
            modifier = Modifier.size(KosmosDimens.iconChrome),
        )
    }
}
