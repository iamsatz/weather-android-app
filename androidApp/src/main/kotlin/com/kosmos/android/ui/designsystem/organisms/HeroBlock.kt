package com.kosmos.android.ui.designsystem.organisms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherSnapshot
import com.kosmos.android.prototype.PrototypeData
import com.kosmos.android.ui.designsystem.atoms.ActivityChipRow
import com.kosmos.android.ui.designsystem.atoms.VerdictArt
import com.kosmos.android.ui.designsystem.tokens.ElderTypography
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosDimens
import com.kosmos.android.ui.designsystem.tokens.KosmosShape
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.android.ui.designsystem.tokens.rememberReduceMotionEnabled

@Composable
fun HeroBlock(
    snapshot: WeatherSnapshot,
    showNumbers: Boolean = false,
    elderMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotionEnabled()
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 500),
        label = "heroFade",
    )
    val topVerdict = snapshot.verdicts.firstOrNull {
        it.priority == VerdictPriority.SEVERE || it.priority == VerdictPriority.ACTION
    } ?: snapshot.verdicts.firstOrNull()
    val heroRes = VerdictArt.heroForSnapshot(topVerdict?.id, snapshot.condition)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(top = KosmosDimens.md),
    ) {
        if (elderMode) {
            Text(
                text = "${snapshot.temp}°",
                style = ElderTypography.temp,
                color = KosmosColor.temp,
            )
            Text(
                text = snapshot.feelsLikePlain,
                style = KosmosTextStyles.body,
                color = KosmosColor.textPrimary,
                modifier = Modifier.padding(bottom = KosmosDimens.sm),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(KosmosColor.accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (!elderMode && topVerdict != null) {
                    Text(
                        text = topVerdict.title,
                        style = KosmosTextStyles.popoverHeroTitle,
                        color = KosmosColor.accent,
                        modifier = Modifier.padding(start = KosmosDimens.lg, end = KosmosDimens.lg),
                    )
                    val window = topVerdict.timeWindow?.label.orEmpty()
                    if (window.isNotBlank()) {
                        Text(
                            text = window,
                            style = KosmosTextStyles.body,
                            color = KosmosColor.textPrimary,
                            modifier = Modifier.padding(
                                start = KosmosDimens.lg,
                                end = KosmosDimens.lg,
                                top = 4.dp,
                            ),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = if (elderMode) 0.dp else KosmosDimens.md)
                        .fillMaxWidth(),
                ) {
                    Image(
                        painter = painterResource(heroRes),
                        contentDescription = topVerdict?.title ?: snapshot.conditionLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(257.dp)
                            .clip(KosmosShape.card),
                        contentScale = ContentScale.Crop,
                    )
                    if (!elderMode) {
                        ActivityChipRow(
                            showWalk = true,
                            showExercise = true,
                            showOutdoor = true,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(KosmosDimens.md),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HeroBlockPreview() {
    KosmosTheme {
        HeroBlock(snapshot = PrototypeData.hyderabadSummerDay)
    }
}
