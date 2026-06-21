package com.kosmos.android.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosTextStyles
import com.kosmos.android.ui.designsystem.tokens.PlayfairFontFamily
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
    val emoji: String,
)

private val pages = listOf(
    OnboardingPage(
        "Weather that tells you what to do",
        "Kosmos turns forecasts into plain-English insights — not just numbers. UV 6 means nothing. 'Sun is brutal — 15 min max' means everything.",
        "✨",
    ),
    OnboardingPage(
        "Your 7 AM daily brief",
        "Every morning around 7, Kosmos sends a short brief: rain timing, heat, air, and what to plan. Turn it on in Settings anytime.",
        "🔔",
    ),
    OnboardingPage(
        "Hyper-local for where you are",
        "Allow location so Kosmos can fetch weather at your spot — not to track you. Without GPS we only guess from your network, which can be wrong.",
        "📍",
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestLocation: () -> Unit,
    onRequestNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(KosmosColor.Gradients.partlyCloudyDay.first, KosmosColor.Gradients.partlyCloudyDay.second),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Skip", color = KosmosColor.textOnGradient)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                val data = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = data.emoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = data.title,
                        fontFamily = PlayfairFontFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = KosmosColor.textOnGradient,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = data.body,
                        style = KosmosTextStyles.verdictDetail,
                        color = KosmosColor.textOnGradient.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    )
                }
            }

            if (pagerState.currentPage == 1) {
                TextButton(onClick = onRequestNotifications) {
                    Text("Enable notifications", color = KosmosColor.textOnGradient)
                }
            }
            if (pagerState.currentPage == 2) {
                TextButton(onClick = onRequestLocation) {
                    Text("Allow location", color = KosmosColor.textOnGradient)
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (pagerState.currentPage < pages.lastIndex) "Next" else "Get started")
            }
        }
    }
}
