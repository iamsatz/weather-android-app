package com.kosmos.shared.engine

import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.mode.UserMode
import com.kosmos.shared.models.DailyData
import com.kosmos.shared.models.HourlyData
import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KosmosEngineStackedTest {

    @Test
    fun dedupeByIdKeepHighestPriority_prefersSevereOverAction() {
        val severe = Verdict("heat", "🔥", "Severe", "detail", VerdictPriority.SEVERE, 0xFF000000)
        val action = Verdict("heat", "☀", "Action", "detail", VerdictPriority.ACTION, 0xFF000000)
        val result = KosmosEngine.dedupeByIdKeepHighestPriority(listOf(action, severe))
        assertEquals(1, result.size)
        assertEquals(VerdictPriority.SEVERE, result.first().priority)
    }

    @Test
    fun evaluateStacked_mergesEmployeeAndFamilyVerdicts() {
        val snapshot = warmDaySnapshot(feelsLike = 30.0)
        val employeeConfig = EvaluateConfig(mode = UserMode.EMPLOYEE, locale = AppLocale.EN)
        val familyConfig = EvaluateConfig(mode = UserMode.FAMILY, locale = AppLocale.EN)
        val stacked = KosmosEngine.evaluateStacked(snapshot, listOf(employeeConfig, familyConfig))
        val employeeOnly = KosmosEngine.evaluate(snapshot, employeeConfig)
        val familyOnly = KosmosEngine.evaluate(snapshot, familyConfig)
        assertTrue(stacked.size >= employeeOnly.size)
        assertTrue(stacked.any { it.id.startsWith("school") || it.id.contains("pickup") || it.id.contains("kids") || familyOnly.any { f -> f.id == it.id } })
    }

    @Test
    fun pregnancySensitivity_addsNightSafetyVerdict() {
        val snapshot = warmDaySnapshot(feelsLike = 28.0, currentHour = 17, sunsetHour = 18)
        val config = EvaluateConfig(
            mode = UserMode.DEFAULT,
            locale = AppLocale.EN,
            sensitivityNightSafety = true,
        )
        val verdicts = KosmosEngine.evaluate(snapshot, config)
        assertTrue(verdicts.any { it.id == "nightSafety" })
    }

    @Test
    fun pregnancySensitivity_lowersHeatThreshold() {
        val snapshot = warmDaySnapshot(feelsLike = 33.0)
        val without = KosmosEngine.evaluate(
            snapshot,
            EvaluateConfig(mode = UserMode.DEFAULT, sensitivityPregnancy = false),
        )
        val withPregnancy = KosmosEngine.evaluate(
            snapshot,
            EvaluateConfig(mode = UserMode.DEFAULT, sensitivityPregnancy = true),
        )
        assertTrue(without.none { it.id == "heat" })
        assertTrue(withPregnancy.any { it.id == "heat" })
    }

    private fun warmDaySnapshot(
        feelsLike: Double,
        currentHour: Int = 10,
        sunsetHour: Int = 18,
    ): WeatherSnapshot {
        val hourly = (0 until 24).map { hour ->
            HourlyData(
                hour = hour,
                label = "$hour",
                temp = feelsLike,
                feelsLike = feelsLike,
                precipProbability = 0,
                uvIndex = if (hour in 10..15) 8.0 else 2.0,
                humidity = 50,
                weatherCode = 0,
                isDay = hour in 6..18,
                isNow = hour == currentHour,
            )
        }
        return WeatherSnapshot(
            latitude = 17.38,
            longitude = 78.48,
            cityName = "Hyderabad",
            dateLabel = "Sun Jun 21",
            temp = feelsLike,
            feelsLike = feelsLike,
            high = feelsLike + 2,
            low = feelsLike - 4,
            humidity = 50,
            isDay = currentHour in 6..18,
            weatherCode = 0,
            uvIndex = 6.0,
            aqi = 40,
            currentHour = currentHour,
            hourly = hourly,
            daily = listOf(
                DailyData("Sun Jun 21", feelsLike + 2, feelsLike - 4, 0, 6, 0, sunsetHour, 30),
            ),
            sunriseHour = 6,
            sunriseMinute = 0,
            sunsetHour = sunsetHour,
            sunsetMinute = 30,
        )
    }
}
