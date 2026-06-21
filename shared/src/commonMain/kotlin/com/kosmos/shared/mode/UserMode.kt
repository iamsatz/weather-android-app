package com.kosmos.shared.mode

enum class ModeAvailability {
    LIVE,
    COMING_SOON,
}

enum class UserMode(val id: String) {
    DEFAULT("default"),
    ELDER("elder"),
    EMPLOYEE("employee"),
    FAMILY("family"),
    PHOTOGRAPHER("photographer"),
    FARMER("farmer"),
    HOMEMAKER("homemaker"),
    ;

    val isPremium: Boolean
        get() = this != DEFAULT && this != ELDER

    val maxVerdicts: Int?
        get() = when (this) {
            ELDER -> 3
            else -> null
        }

    val insightsLabelKey: String
        get() = when (this) {
            DEFAULT -> "insights"
            ELDER -> "today_summary"
            EMPLOYEE -> "work_day"
            FAMILY -> "family_insights"
            PHOTOGRAPHER -> "shoot_windows"
            FARMER -> "crop_insights"
            HOMEMAKER -> "home_kitchen"
        }

    val usesBaseIdToggles: Boolean
        get() = this == DEFAULT

    val isTakeoverMode: Boolean
        get() = this == FARMER || this == ELDER

    val isStackableLens: Boolean
        get() = !isTakeoverMode

    companion object {
        fun fromId(id: String): UserMode =
            entries.firstOrNull { it.id == id } ?: DEFAULT

        val selectableModes = listOf(DEFAULT, ELDER, EMPLOYEE, FAMILY, PHOTOGRAPHER, FARMER, HOMEMAKER)

        fun isActivatable(id: String): Boolean =
            id in selectableModes.map { it.id }
    }
}

data class ModeCatalogEntry(
    val id: String,
    val userMode: UserMode?,
    val emoji: String,
    val nameKey: String,
    val descKey: String,
    val availability: ModeAvailability,
    val insightKeys: List<String> = emptyList(),
)

object ModeCatalog {
    val live = listOf(
        ModeCatalogEntry("default", UserMode.DEFAULT, "🌤", "mode_default", "mode_default_desc", ModeAvailability.LIVE, listOf("mode_insight_default")),
        ModeCatalogEntry("employee", UserMode.EMPLOYEE, "💼", "mode_employee", "mode_employee_desc", ModeAvailability.LIVE, listOf("mode_insight_commute", "mode_insight_lunch", "mode_insight_gym")),
        ModeCatalogEntry("family", UserMode.FAMILY, "👨‍👩‍👧", "mode_family", "mode_family_desc", ModeAvailability.LIVE, listOf("mode_insight_school", "mode_insight_pickup", "mode_insight_kids_air")),
        ModeCatalogEntry("elder", UserMode.ELDER, "👴", "mode_elder", "mode_elder_desc", ModeAvailability.LIVE, listOf("mode_insight_elder_simple")),
        ModeCatalogEntry("photographer", UserMode.PHOTOGRAPHER, "📸", "mode_photographer", "mode_photographer_desc", ModeAvailability.LIVE, listOf("mode_insight_golden", "mode_insight_fog", "mode_insight_stars")),
        ModeCatalogEntry("farmer", UserMode.FARMER, "🌾", "mode_farmer", "mode_farmer_desc", ModeAvailability.LIVE, listOf("mode_insight_spray", "mode_insight_harvest", "mode_insight_monsoon")),
        ModeCatalogEntry("homemaker", UserMode.HOMEMAKER, "🏠", "mode_homemaker", "mode_homemaker_desc", ModeAvailability.LIVE, listOf("mode_insight_drying", "mode_insight_storm", "mode_insight_pickle")),
    )

    val comingSoon = listOf(
        ModeCatalogEntry("students", null, "🎓", "mode_students", "mode_students_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_exam", "mode_insight_bus")),
        ModeCatalogEntry("delivery_riders", null, "🛵", "mode_riders", "mode_riders_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_heat_fatigue", "mode_insight_rain_gear")),
        ModeCatalogEntry("daily_wage", null, "👷", "mode_daily_wage", "mode_daily_wage_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_work_stop", "mode_insight_heat_break")),
        ModeCatalogEntry("street_vendors", null, "🛒", "mode_vendors", "mode_vendors_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_footfall", "mode_insight_spoilage")),
        ModeCatalogEntry("drivers", null, "🚗", "mode_drivers", "mode_drivers_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_fog_highway", "mode_insight_departure")),
        ModeCatalogEntry("fishermen", null, "🎣", "mode_fishermen", "mode_fishermen_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_gale", "mode_insight_waves")),
        ModeCatalogEntry("athletes", null, "🏃", "mode_athletes", "mode_athletes_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_run_window", "mode_insight_aqi_jog")),
        ModeCatalogEntry("events_caterers", null, "🎪", "mode_events", "mode_events_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_tent_wind", "mode_insight_food_storage")),
        ModeCatalogEntry("religious_festival", null, "🪔", "mode_religious", "mode_religious_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_procession", "mode_insight_diya")),
        ModeCatalogEntry("gardeners", null, "🌱", "mode_gardeners", "mode_gardeners_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_watering", "mode_insight_frost")),
        ModeCatalogEntry("solar_ev", null, "🔋", "mode_solar_ev", "mode_solar_ev_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_solar", "mode_insight_ev_range")),
        ModeCatalogEntry("pet_owners", null, "🐕", "mode_pets", "mode_pets_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_paw_burn", "mode_insight_pet_walk")),
        ModeCatalogEntry("travel_overlay", null, "✈️", "mode_travel", "mode_travel_desc", ModeAvailability.COMING_SOON, listOf("mode_insight_pack", "mode_insight_home_split")),
    )

    @Deprecated("Use live", ReplaceWith("live"))
    val all = live.map { entry ->
        ModeMeta(
            mode = entry.userMode ?: UserMode.DEFAULT,
            emoji = entry.emoji,
            nameKey = entry.nameKey,
            descKey = entry.descKey,
            phaseKey = if (entry.availability == ModeAvailability.LIVE) "phase_v1" else "coming_soon",
        )
    }
}

data class ModeMeta(
    val mode: UserMode,
    val emoji: String,
    val nameKey: String,
    val descKey: String,
    val phaseKey: String,
)
