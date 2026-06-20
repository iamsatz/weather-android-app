package com.kosmos.shared.i18n

data class FestivalInfo(
    val id: String,
    val name: String,
    val greeting: String,
)

object FestivalContext {

    private data class FestivalRule(
        val id: String,
        val month: Int,
        val dayStart: Int,
        val dayEnd: Int,
    )

    private val rules = listOf(
        FestivalRule("sankranti", 1, 13, 16),
        FestivalRule("holi", 3, 10, 14),
        FestivalRule("diwali", 10, 28, 5),
    )

    fun currentFestival(month: Int, day: Int): FestivalInfo? {
        for (rule in rules) {
            if (rule.id == "diwali") {
                if (month == 10 && day >= rule.dayStart || month == 11 && day <= rule.dayEnd) {
                    return info(rule.id)
                }
            } else if (month == rule.month && day in rule.dayStart..rule.dayEnd) {
                return info(rule.id)
            }
        }
        return null
    }

    fun festivalBrief(locale: AppLocale, month: Int, day: Int): String? {
        val festival = currentFestival(month, day) ?: return null
        return LocaleStrings.festivalGreeting(festival.id, locale)
    }

    private fun info(id: String): FestivalInfo = when (id) {
        "sankranti" -> FestivalInfo("sankranti", "Sankranti", "Happy Sankranti")
        "holi" -> FestivalInfo("holi", "Holi", "Happy Holi")
        "diwali" -> FestivalInfo("diwali", "Diwali", "Happy Diwali")
        else -> FestivalInfo(id, id, id)
    }
}
