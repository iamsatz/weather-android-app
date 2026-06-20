package com.kosmos.shared.i18n

enum class AppLocale(val code: String, val displayName: String, val nativeName: String) {
    EN("en", "English", "English"),
    HI("hi", "Hindi", "हिन्दी"),
    TE("te", "Telugu", "తెలుగు"),
    TA("ta", "Tamil", "தமிழ்"),
    ;

    companion object {
        fun fromCode(code: String): AppLocale =
            entries.firstOrNull { it.code == code } ?: EN
    }
}
