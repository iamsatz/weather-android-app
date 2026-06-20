package com.kosmos.android.ui.designsystem.tokens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntSize

object KosmosMotion {
    const val breatheDurationMs = 2000
    const val cellExpandMs = 250
    const val fadeMs = 200
    const val conditionCrossfadeMs = 800
    const val refreshSpinMs = 900

    fun contentSizeSpec(): FiniteAnimationSpec<IntSize> =
        tween(cellExpandMs, easing = FastOutSlowInEasing)

    fun fadeSpec(): FiniteAnimationSpec<Float> =
        tween(fadeMs, easing = FastOutSlowInEasing)
}
