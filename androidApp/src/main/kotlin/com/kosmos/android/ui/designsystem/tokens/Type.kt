package com.kosmos.android.ui.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val KosmosTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        lineHeight = 80.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
)

object KosmosTextStyles {
    val heroTitle = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )
    val popoverHeroTitle = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
    )
    val cardTitle = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    )
    val temp = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        lineHeight = 80.sp,
    )
    val rowTitle = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val body = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val label = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    )
    val caption = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val time = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )

    val compactHeroTitle = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
    )
    val compactCardTitle = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 19.sp,
    )
    val compactRowTitle = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    )
    val compactBody = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val compactLabel = TextStyle(
        fontFamily = PublicSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp,
    )
    val widgetTemp = TextStyle(
        fontFamily = SolwayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
    )

    val locationHeader = compactLabel.copy(letterSpacing = 0.sp)
    val dateHeader = body
    val heroVerdictLabel = rowTitle
    val cityPill = compactRowTitle
    val conditionLabel = rowTitle
    val feelsLike = body
    val hiLo = caption
    val verdictTitle = cardTitle.copy(fontSize = 16.sp, lineHeight = 19.sp)
    val verdictDetail = compactBody
    val hourlyTime = compactLabel
    val hourlyTemp = time.copy(fontWeight = FontWeight.SemiBold)
    val settingsTitle = rowTitle
    val settingsSubtitle = body
}
