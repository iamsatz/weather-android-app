package com.kosmos.shared.engine

import com.kosmos.shared.alert.ImdAlert
import com.kosmos.shared.alert.ImdAlertCatalog
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.i18n.FestivalContext
import com.kosmos.shared.i18n.VerdictLocalizer
import com.kosmos.shared.mode.CommuteAdvice
import com.kosmos.shared.mode.CommuteMode
import com.kosmos.shared.mode.UserMode
import com.kosmos.shared.farmer.FarmerProfile
import com.kosmos.shared.farmer.FarmerVerdictEngine
import com.kosmos.shared.farmer.SoilData
import com.kosmos.shared.models.Verdict
import com.kosmos.shared.models.VerdictPriority
import com.kosmos.shared.models.WeatherSnapshot
import com.kosmos.shared.models.upcomingHours

data class EvaluateConfig(
    val mode: UserMode = UserMode.DEFAULT,
    val locale: AppLocale = AppLocale.EN,
    val disabledBaseIds: Set<String> = emptySet(),
    val hiddenExactIds: Set<String> = emptySet(),
    val commuteModes: Set<CommuteMode> = emptySet(),
    val month: Int = 1,
    val day: Int = 1,
    val farmerProfile: FarmerProfile? = null,
    val soilData: SoilData? = null,
    val region: String = "Telangana",
    val isKosmosPlus: Boolean = false,
)

object KosmosEngine {

    fun evaluate(snapshot: WeatherSnapshot, config: EvaluateConfig): List<Verdict> {
        val imd = imdVerdicts(config.region, config.month, config.day)
        val health = HealthVerdictEngine.proVerdicts(snapshot, config.isKosmosPlus)

        val raw = when (config.mode) {
            UserMode.DEFAULT -> VerdictEngine.evaluate(snapshot, config.disabledBaseIds)
            UserMode.ELDER -> VerdictEngine.evaluate(snapshot, config.disabledBaseIds)
            UserMode.EMPLOYEE -> ModeVerdictEngine.employeeVerdicts(snapshot, config.hiddenExactIds)
            UserMode.FAMILY -> ModeVerdictEngine.familyVerdicts(snapshot, config.hiddenExactIds)
            UserMode.PHOTOGRAPHER -> ModeVerdictEngine.photographerVerdicts(snapshot, config.hiddenExactIds)
            UserMode.HOMEMAKER -> ModeVerdictEngine.homemakerVerdicts(snapshot, config.hiddenExactIds)
            UserMode.FARMER -> {
                val profile = config.farmerProfile ?: FarmerProfile()
                val soil = config.soilData ?: SoilData(null, null)
                FarmerVerdictEngine.evaluate(snapshot, soil, profile, config.region, config.month, config.day)
            }
        }

        val merged = imd + health + raw

        val filtered = when (config.mode) {
            UserMode.DEFAULT, UserMode.ELDER -> merged
            else -> merged.filter { it.id !in config.hiddenExactIds }
        }

        val displayFiltered = VerdictEngine.filterForDisplay(filtered)

        val capped = config.mode.maxVerdicts?.let { max ->
            displayFiltered.take(max)
        } ?: displayFiltered

        val hasSecondary = snapshot.fusionMeta?.sources?.contains("Tomorrow.io") == true
        val next12 = snapshot.upcomingHours(12)

        val withCommute = capped.map { verdict ->
            var detail = CommuteAdvice.appendToVerdict(
                verdictId = verdict.id,
                detail = verdict.detail,
                commuteModes = config.commuteModes,
                localeCode = config.locale.code,
            )
            if (config.mode == UserMode.DEFAULT) {
                when (verdict.id) {
                    "raincoat", "umbrella" -> {
                        ConfidenceEngine.rainConfidence(next12, hasSecondary)?.let { conf ->
                            detail = "$detail · ${conf.copy}"
                        }
                    }
                    "avoidHours", "heat" -> {
                        ConfidenceEngine.tempConfidence(next12, hasSecondary)?.let { conf ->
                            detail = "$detail · ${conf.copy}"
                        }
                    }
                }
            }
            verdict.copy(detail = detail)
        }

        val localized = VerdictLocalizer.localizeAll(withCommute, config.locale)

        val festivalLine = FestivalContext.festivalBrief(config.locale, config.month, config.day)
        if (festivalLine != null && localized.isNotEmpty()) {
            val first = localized.first()
            return VerdictEngine.sortChronologically(
                listOf(first.copy(detail = "$festivalLine ${first.detail}")) + localized.drop(1),
            )
        }

        return VerdictEngine.sortChronologically(localized)
    }

    private fun imdVerdicts(region: String, month: Int, day: Int): List<Verdict> =
        ImdAlertCatalog.activeAlerts(region, month, day).map { alert ->
            Verdict(
                id = alert.id,
                emoji = when (alert.severity) {
                    ImdAlert.Severity.WARNING -> "🌀"
                    ImdAlert.Severity.WATCH -> "⚠️"
                    ImdAlert.Severity.ADVISORY -> "📢"
                },
                title = alert.title,
                detail = alert.detail,
                priority = when (alert.severity) {
                    ImdAlert.Severity.WARNING -> VerdictPriority.SEVERE
                    ImdAlert.Severity.WATCH -> VerdictPriority.ACTION
                    ImdAlert.Severity.ADVISORY -> VerdictPriority.ACTION
                },
                accentColor = 0xFF3F8CD9,
            )
        }
}
