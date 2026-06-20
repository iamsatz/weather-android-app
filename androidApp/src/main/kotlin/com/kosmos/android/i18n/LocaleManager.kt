package com.kosmos.android.i18n

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kosmos.shared.i18n.AppLocale

object LocaleManager {

    fun applyLocale(context: Context, locale: AppLocale) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(locale.code),
        )
    }

    fun currentLocale(context: Context): AppLocale {
        val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (tags.isBlank()) return AppLocale.EN
        return AppLocale.fromCode(tags.substringBefore("-"))
    }
}
