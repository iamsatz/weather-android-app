package com.kosmos.shared.alert

data class ImdAlert(
    val id: String,
    val title: String,
    val detail: String,
    val region: String,
    val severity: Severity,
) {
    enum class Severity { WATCH, WARNING, ADVISORY }
}

object ImdAlertCatalog {

    fun activeAlerts(region: String, month: Int, day: Int): List<ImdAlert> {
        val alerts = mutableListOf<ImdAlert>()

        if (month == 6 && day in 1..15 && region in listOf("Kerala", "Tamil Nadu", "Karnataka")) {
            alerts += ImdAlert(
                id = "imd.monsoon.onset",
                title = "Monsoon onset window",
                detail = "IMD expects monsoon advance over south peninsula — watch for heavy rain bursts",
                region = region,
                severity = ImdAlert.Severity.WATCH,
            )
        }

        if (month in 10..12 && region in listOf("Andhra Pradesh", "Tamil Nadu", "Odisha", "West Bengal")) {
            alerts += ImdAlert(
                id = "imd.cyclone.season",
                title = "Cyclone season active",
                detail = "Bay of Bengal cyclone season — track IMD bulletins if coastal",
                region = region,
                severity = ImdAlert.Severity.ADVISORY,
            )
        }

        if (month in 5..6 && region in listOf("Telangana", "Andhra Pradesh", "Maharashtra")) {
            alerts += ImdAlert(
                id = "imd.heatwave",
                title = "Pre-monsoon heat watch",
                detail = "IMD heatwave thresholds likely before monsoon break — limit afternoon outdoor work",
                region = region,
                severity = ImdAlert.Severity.WATCH,
            )
        }

        return alerts
    }
}
