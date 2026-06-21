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
import com.kosmos.shared.mode.EmployeeProfile
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
    val employeeProfile: EmployeeProfile? = null,
    val workWeather: WeatherSnapshot? = null,
    val soilData: SoilData? = null,
    val region: String = "Telangana",
    val isKosmosPlus: Boolean = false,
    val sensitivityAsthma: Boolean = false,
    val sensitivityKids: Boolean = false,
    val sensitivityPregnancy: Boolean = false,
    val sensitivityNightSafety: Boolean = false,
    val sensitivityWoman: Boolean = false,
)

object KosmosEngine {

    fun evaluate(snapshot: WeatherSnapshot, config: EvaluateConfig): List<Verdict> {
        val raw = evaluateRaw(snapshot, config)
        val merged = if (config.mode == UserMode.DEFAULT || config.mode == UserMode.ELDER) {
            raw
        } else {
            dedupeByIdKeepHighestPriority(raw + sensitivityOverlays(snapshot, config))
        }
        return finalizeVerdicts(snapshot, merged, config, applyModeCap = true)
    }

    fun evaluateStacked(snapshot: WeatherSnapshot, configs: List<EvaluateConfig>): List<Verdict> {
        if (configs.isEmpty()) return emptyList()
        if (configs.size == 1) return evaluate(snapshot, configs.first())

        val baseConfig = configs.first()
        val raw = configs.flatMap { evaluateRaw(snapshot, it) }
        val withOverlays = raw + sensitivityOverlays(snapshot, baseConfig)
        val deduped = dedupeByIdKeepHighestPriority(withOverlays)
        return finalizeVerdicts(snapshot, deduped, baseConfig, applyModeCap = false)
    }

    private fun evaluateRaw(snapshot: WeatherSnapshot, config: EvaluateConfig): List<Verdict> {
        val imd = imdVerdicts(config.region, config.month, config.day)
        val health = HealthVerdictEngine.proVerdicts(snapshot, config.isKosmosPlus)

        val raw = when (config.mode) {
            UserMode.DEFAULT -> VerdictEngine.evaluate(
                snapshot,
                config.disabledBaseIds,
                config.sensitivityAsthma,
                config.sensitivityKids,
                config.sensitivityPregnancy,
                config.sensitivityNightSafety,
                config.sensitivityWoman,
            )
            UserMode.ELDER -> VerdictEngine.evaluate(
                snapshot,
                config.disabledBaseIds,
                config.sensitivityAsthma,
                config.sensitivityKids,
                config.sensitivityPregnancy,
                config.sensitivityNightSafety,
                config.sensitivityWoman,
            )
            UserMode.EMPLOYEE -> ModeVerdictEngine.employeeVerdicts(
                snapshot,
                config.hiddenExactIds,
                config.employeeProfile,
                config.workWeather,
            )
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

        return when (config.mode) {
            UserMode.DEFAULT, UserMode.ELDER -> merged
            else -> merged.filter { it.id !in config.hiddenExactIds }
        }
    }

    private fun sensitivityOverlays(snapshot: WeatherSnapshot, config: EvaluateConfig): List<Verdict> {
        if (config.mode == UserMode.DEFAULT || config.mode == UserMode.ELDER) return emptyList()
        return VerdictEngine.sensitivityOverlayVerdicts(
            snapshot,
            config.disabledBaseIds,
            config.sensitivityPregnancy,
            config.sensitivityNightSafety,
        )
    }

    private fun finalizeVerdicts(
        snapshot: WeatherSnapshot,
        raw: List<Verdict>,
        config: EvaluateConfig,
        applyModeCap: Boolean,
    ): List<Verdict> {
        val displayFiltered = VerdictEngine.filterForDisplay(raw)

        val capped = if (applyModeCap) {
            config.mode.maxVerdicts?.let { max -> displayFiltered.take(max) } ?: displayFiltered
        } else {
            displayFiltered
        }

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

    fun mergeVerdictExtras(base: List<Verdict>, extras: List<Verdict>): List<Verdict> =
        dedupeByIdKeepHighestPriority(base + extras)

    internal fun dedupeByIdKeepHighestPriority(verdicts: List<Verdict>): List<Verdict> {
        val byId = linkedMapOf<String, Verdict>()
        for (verdict in verdicts) {
            val existing = byId[verdict.id]
            if (existing == null || verdict.priority.ordinal < existing.priority.ordinal) {
                byId[verdict.id] = verdict
            }
        }
        return byId.values.toList()
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
