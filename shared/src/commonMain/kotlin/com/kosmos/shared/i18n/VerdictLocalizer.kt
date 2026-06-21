package com.kosmos.shared.i18n

import com.kosmos.shared.models.Verdict

object VerdictLocalizer {

    fun localize(verdict: Verdict, locale: AppLocale): Verdict {
        if (locale == AppLocale.EN) return verdict
        return verdict.copy(
            title = LocaleStrings.verdictTitle(verdict.id, locale, verdict.title),
            detail = LocaleStrings.verdictDetail(verdict.id, locale, verdict.detail),
        )
    }

    fun localizeAll(verdicts: List<Verdict>, locale: AppLocale): List<Verdict> =
        verdicts.map { localize(it, locale) }
}
