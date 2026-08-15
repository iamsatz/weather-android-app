package com.kosmos.android.ui.designsystem.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kosmos.android.model.Verdict
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles

@Composable
fun InsightCard(
    verdict: Verdict,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val listArt = VerdictArt.listArt(verdict.id)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(KosmosShape.card)
            .background(Color.White)
            .border(1.dp, KosmosColor.border, KosmosShape.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = KosmosDimens.sm, vertical = 4.dp)
            .semantics { contentDescription = "${verdict.title}. ${verdict.detail}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(KosmosShape.card)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            if (listArt != null) {
                Image(
                    painter = painterResource(listArt),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Icon(
                    painter = painterResource(VerdictArt.icon(verdict.id)),
                    contentDescription = null,
                    tint = Color(verdict.accentColor),
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = KosmosDimens.sm, end = KosmosDimens.md, vertical = KosmosDimens.xs),
        ) {
            Text(
                text = verdict.title,
                style = KosmosTextStyles.verdictTitle,
                color = KosmosColor.textPrimary,
            )
            Text(
                text = verdict.timeWindow?.label?.takeIf { it.isNotBlank() } ?: verdict.detail,
                style = KosmosTextStyles.compactLabel,
                color = KosmosColor.insightTime,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        trailing?.invoke()
    }
}
