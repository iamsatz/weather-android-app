package com.kosmos.shared.mode

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
            HOMEMAKER -> "home_insights"
        }

    val usesBaseIdToggles: Boolean
        get() = this == DEFAULT

    companion object {
        fun fromId(id: String): UserMode =
            entries.firstOrNull { it.id == id } ?: DEFAULT

        val selectableModes = listOf(DEFAULT, ELDER, EMPLOYEE, FAMILY, PHOTOGRAPHER, FARMER, HOMEMAKER)
    }
}

data class ModeMeta(
    val mode: UserMode,
    val emoji: String,
    val nameKey: String,
    val descKey: String,
    val phaseKey: String,
)

object ModeCatalog {
    val all = listOf(
        ModeMeta(UserMode.DEFAULT, "🌤", "mode_default", "mode_default_desc", "phase_v1"),
        ModeMeta(UserMode.EMPLOYEE, "💼", "mode_employee", "mode_employee_desc", "phase_v2"),
        ModeMeta(UserMode.FAMILY, "👨‍👩‍👧", "mode_family", "mode_family_desc", "phase_v2"),
        ModeMeta(UserMode.ELDER, "👴", "mode_elder", "mode_elder_desc", "phase_v13"),
        ModeMeta(UserMode.PHOTOGRAPHER, "📸", "mode_photographer", "mode_photographer_desc", "phase_v2"),
        ModeMeta(UserMode.FARMER, "🌾", "mode_farmer", "mode_farmer_desc", "phase_v3"),
        ModeMeta(UserMode.HOMEMAKER, "🏠", "mode_homemaker", "mode_homemaker_desc", "phase_v2"),
    )
}
