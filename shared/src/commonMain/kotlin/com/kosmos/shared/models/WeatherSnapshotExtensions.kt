package com.kosmos.shared.models

fun WeatherSnapshot.upcomingHours(count: Int): List<HourlyData> =
    hourly.drop(nowIndex.coerceIn(0, hourly.size)).take(count)

fun WeatherSnapshot.upcomingHours(): List<HourlyData> =
    hourly.drop(nowIndex.coerceIn(0, hourly.size))

fun WeatherSnapshot.hoursAhead(hourlyIndex: Int): Int = hourlyIndex - nowIndex
