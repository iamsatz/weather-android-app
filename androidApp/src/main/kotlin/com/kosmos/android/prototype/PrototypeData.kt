package com.kosmos.android.prototype

import com.kosmos.android.model.HourlyForecast
import com.kosmos.android.model.TimeWindow
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.VerdictPriority
import com.kosmos.android.model.WeatherCondition
import com.kosmos.android.model.WeatherSnapshot

object PrototypeData {

    val hyderabadSummerDay = WeatherSnapshot(
        city = "Hyderabad",
        country = "India",
        locationLine = "Gachibowli, Hyderabad",
        locationHeadline = "Gachibowli",
        locationDetail = "Hyderabad · India",
        dateLabel = "Thu Jun 19",
        temp = 34,
        tempCelsius = 34.0,
        latitude = 17.385,
        longitude = 78.487,
        feelsLike = 38,
        feelsLikePlain = "Very hot",
        high = 36,
        low = 26,
        condition = WeatherCondition.PARTLY_CLOUDY,
        conditionLabel = "Partly cloudy",
        aqiLabel = "Good",
        aqiColor = 0xFF4DC85A,
        uvIndex = 8.0,
        aqiValue = 42,
        humidity = 68,
        hourly = listOf(
            HourlyForecast(14, "2PM", 34, "🌤", isNow = true),
            HourlyForecast(15, "3PM", 33, "🌤"),
            HourlyForecast(16, "4PM", 31, "☔"),
            HourlyForecast(17, "5PM", 30, "☔"),
            HourlyForecast(18, "6PM", 29, "🌤"),
            HourlyForecast(19, "7PM", 28, "🌤"),
            HourlyForecast(20, "8PM", 27, "🌙"),
            HourlyForecast(21, "9PM", 26, "🌙"),
            HourlyForecast(22, "10PM", 25, "🌙"),
            HourlyForecast(23, "11PM", 24, "🌙"),
            HourlyForecast(0, "12AM", 24, "🌙"),
            HourlyForecast(1, "1AM", 23, "🌙"),
        ),
        verdicts = listOf(
            Verdict(
                id = "raincoat",
                emoji = "☔",
                title = "Take a raincoat",
                detail = "78% rain · 4–6 PM — carry it before you head out",
                priority = VerdictPriority.SEVERE,
                accentColor = 0xFF1A5CB3,
                timeWindow = TimeWindow(16, 18, "4–6 PM"),
            ),
            Verdict(
                id = "bestWalk.evening",
                emoji = "🚶",
                title = "Best walk: 6–7 PM",
                detail = "6 PM–7 PM · ~29°, gentlest air after the rain",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3FA67F,
                timeWindow = TimeWindow(18, 19, "6–7 PM"),
            ),
            Verdict(
                id = "mosquito",
                emoji = "🦟",
                title = "Mosquito hour — cover up",
                detail = "Sleeves, repellent, screens. Peak biting till 9 PM.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF8C4D80,
                timeWindow = TimeWindow(17, 21, "5–9 PM"),
            ),
            Verdict(
                id = "hydration",
                emoji = "💧",
                title = "Drink 3L water today",
                detail = "Hot. Keep a bottle close, sip every hour.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFF3F8CD9,
            ),
            Verdict(
                id = "vitaminD.morning",
                emoji = "🌤",
                title = "Vitamin D window was 9:30–9:45",
                detail = "UV 5 · 15 min was enough — tomorrow's window opens at 9 AM",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFD98C33,
                timeWindow = TimeWindow(9, 10, "9:30–9:45"),
            ),
            Verdict(
                id = "goldenHour.sunset",
                emoji = "🌅",
                title = "Golden hour: 6:42 PM",
                detail = "Best light for photos — 60 min before sunset",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFF28C4D,
                timeWindow = TimeWindow(18, 19, "6:42 PM"),
            ),
            Verdict(
                id = "avoidHours",
                emoji = "🔥",
                title = "Avoid outdoors 11 AM–4 PM",
                detail = "38° + climbing. Plan errands before or after.",
                priority = VerdictPriority.ACTION,
                accentColor = 0xFFC74D33,
                timeWindow = TimeWindow(11, 16, "11 AM–4 PM"),
            ),
        ),
        nowcast = "Rain likely in your area (Gachibowli) in about an hour — carry a raincoat if you head out.",
        nowcastIsWet = true,
        updatedMinutesAgo = 2,
        hasLiveLocation = true,
    )

    val allVerdictToggles = listOf(
        "raincoat" to "Rain alerts",
        "umbrella" to "Umbrella reminders",
        "heat" to "Heat warnings",
        "sunProtection" to "Sun protection",
        "cold" to "Cold weather",
        "air" to "Air quality",
        "vitaminD" to "Vitamin D windows",
        "bestWalk" to "Best walk times",
        "avoidHours" to "Avoid outdoors",
        "canJog" to "Jog conditions",
        "hydration" to "Hydration goals",
        "mosquito" to "Mosquito hour",
        "openWindows" to "Open windows",
        "closeWindows" to "Keep windows shut",
        "laundry" to "Laundry day",
        "goldenHour" to "Golden hour",
        "coolerTomorrow" to "Cooler tomorrow",
    )

    val cities = listOf(
        "Hyderabad, India",
        "Bengaluru, India",
        "Mumbai, India",
        "Delhi, India",
        "Chennai, India",
        "London, UK",
        "New York, USA",
        "Tokyo, Japan",
    )
}
