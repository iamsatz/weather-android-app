package com.kosmos.shared.mode

enum class CommuteMode(val id: String, val emoji: String) {
    BIKE("bike", "🛵"),
    BUS("bus", "🚌"),
    CAR("car", "🚗"),
    WALK("walk", "🚶"),
    ;

    companion object {
        fun fromId(id: String): CommuteMode? = entries.firstOrNull { it.id == id }

        fun fromIds(ids: Set<String>): Set<CommuteMode> =
            ids.mapNotNull { fromId(it) }.toSet()
    }
}

object CommuteAdvice {

    private val rainAdvice = mapOf(
        CommuteMode.BIKE to mapOf(
            AppLocaleKey.EN to "Auto-rickshaw or bus might be safer if roads get slick.",
            AppLocaleKey.HI to "सड़क फिसलन भरी हो तो ऑटो या बस सुरक्षित रहेगी।",
            AppLocaleKey.TE to "రోడ్ వెత్తలు పడితే ఆటో లేదా బస్సు బాగుంటుంది.",
            AppLocaleKey.TA to "சாலை வழுக்கினால் ஆட்டோ அல்லது பஸ் பாதுகாப்பானது.",
        ),
        CommuteMode.BUS to mapOf(
            AppLocaleKey.EN to "Carry a foldable umbrella for the walk to the stop.",
            AppLocaleKey.HI to "स्टॉप तक पैदल जाने के लिए छोटा छाता रखें।",
            AppLocaleKey.TE to "బస్ స్టాప్‌కు నడవడానికి చిన్న గొడుగు తీసుకోండి.",
            AppLocaleKey.TA to "பஸ் நிறுத்தம் வரை நடக்க சிறிய குடை வைத்துக்கொள்ளுங்கள்.",
        ),
        CommuteMode.CAR to mapOf(
            AppLocaleKey.EN to "Wipers on — visibility drops fast in evening showers.",
            AppLocaleKey.HI to "वाइपर चालू रखें — शाम की बारिश में दृश्यता कम हो जाती है।",
            AppLocaleKey.TE to "వైపర్లు ఆన్ చేయండి — సాయంత్రం వర్షంలో దృష్టి త్వరగా తగ్గుతుంది.",
            AppLocaleKey.TA to "வைப்பர் ஆன் — மாலை மழையில் பார்வை வேகமாக குறையும்.",
        ),
        CommuteMode.WALK to mapOf(
            AppLocaleKey.EN to "Waterproof shoes — puddles stay till late evening.",
            AppLocaleKey.HI to "वॉटरप्रूफ जूते — शाम तक गड्ढे भरे रहेंगे।",
            AppLocaleKey.TE to "వాటర్‌ప్రూఫ్ షూస్ — సాయంత్రం వరకు గుంతలు ఉంటాయి.",
            AppLocaleKey.TA to "தண்ணீர் புகா காலணிகள் — மாலை வரை குழிகள் இருக்கும்.",
        ),
    )

    private val heatAdvice = mapOf(
        CommuteMode.BIKE to mapOf(
            AppLocaleKey.EN to "Carry water — two-wheeler heat hits harder.",
            AppLocaleKey.HI to "पानी साथ रखें — बाइक पर गर्मी ज़्यादा लगती है।",
            AppLocaleKey.TE to "నీళ్ళు తీసుకోండి — బైక్ మీద వేడి ఎక్కువ.",
            AppLocaleKey.TA to "தண்ணீர் எடுத்துச் செல்லுங்கள் — பைக்கில் வெப்பம் அதிகம்.",
        ),
        CommuteMode.BUS to mapOf(
            AppLocaleKey.EN to "Stand in shade at the stop — midday sun is brutal.",
            AppLocaleKey.HI to "स्टॉप पर छाया में खड़े रहें — दोपहर की धूप कड़वी है।",
            AppLocaleKey.TE to "బస్ స్టాప్‌లో నీడలో నిలబడండి — మధ్యాహ్నం ఎండ మండుతుంది.",
            AppLocaleKey.TA to "பஸ் நிறுத்தத்தில் நிழலில் நில்லுங்கள் — மதிய வெயில் கடுமைானது.",
        ),
        CommuteMode.CAR to mapOf(
            AppLocaleKey.EN to "Park in shade — cabin hits 50° in 20 min.",
            AppLocaleKey.HI to "छाया में पार्क करें — 20 मिनट में केबिन 50° हो जाती है।",
            AppLocaleKey.TE to "నీడలో పార్క్ చేయండి — 20 నిమిషాల్లో క్యాబిన్ 50° అవుతుంది.",
            AppLocaleKey.TA to "நிழலில் நிறுத்துங்கள் — 20 நிமிடத்தில் கார் 50° ஆகும்.",
        ),
        CommuteMode.WALK to mapOf(
            AppLocaleKey.EN to "Walk shaded routes — hat and water non-negotiable.",
            AppLocaleKey.HI to "छायादार रास्ते चुनें — टोपी और पानी ज़रूरी।",
            AppLocaleKey.TE to "నీడ మార్గాల్లో నడవండి — టోపీ, నీళ్ళు తప్పనిసరి.",
            AppLocaleKey.TA to "நிழல் பாதைகளில் நடக்குங்கள் — தொப்பி, தண்ணீர் அவசியம்.",
        ),
    )

    fun appendToVerdict(
        verdictId: String,
        detail: String,
        commuteModes: Set<CommuteMode>,
        localeCode: String,
    ): String {
        if (commuteModes.isEmpty()) return detail
        val locale = AppLocaleKey.fromCode(localeCode)
        val baseId = verdictId.substringBefore(".")
        val adviceMap = when (baseId) {
            "raincoat", "umbrella" -> rainAdvice
            "heat" -> heatAdvice
            else -> return detail
        }
        val lines = commuteModes.mapNotNull { mode ->
            adviceMap[mode]?.get(locale)
        }
        if (lines.isEmpty()) return detail
        return detail + " " + lines.joinToString(" ")
    }
}

private enum class AppLocaleKey(val code: String) {
    EN("en"), HI("hi"), TE("te"), TA("ta");

    companion object {
        fun fromCode(code: String): AppLocaleKey =
            entries.firstOrNull { it.code == code } ?: EN
    }
}
