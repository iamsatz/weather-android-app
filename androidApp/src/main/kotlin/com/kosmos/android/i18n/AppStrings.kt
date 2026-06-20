package com.kosmos.android.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.kosmos.shared.i18n.AppLocale
import com.kosmos.shared.i18n.LocaleStrings

val LocalAppLocale = staticCompositionLocalOf { AppLocale.EN }

object S {
    @Composable
    fun get(key: String): String {
        val locale = LocalAppLocale.current
        return LocaleStrings.ui(key, locale)
    }
}

@Composable
fun localized(key: String): String = S.get(key)
