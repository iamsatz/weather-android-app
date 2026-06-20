package com.kosmos.android.service

import android.content.Context
import android.speech.tts.TextToSpeech
import com.kosmos.android.model.Verdict
import com.kosmos.android.model.WeatherSnapshot
import java.util.Locale

class BriefReader(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
    }

    fun readBrief(snapshot: WeatherSnapshot) {
        if (!ready) return
        val locale = when (snapshot.localeCode) {
            "hi" -> Locale("hi", "IN")
            "te" -> Locale("te", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale.US
        }
        tts?.language = locale
        val text = buildBriefText(snapshot)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kosmos_brief")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }

    private fun buildBriefText(snapshot: WeatherSnapshot): String {
        val parts = mutableListOf<String>()
        parts += "${snapshot.locationLine}. ${snapshot.temp} degrees. ${snapshot.conditionLabel}."
        snapshot.verdicts.take(3).forEach { v ->
            parts += "${v.title}. ${v.detail}"
        }
        return parts.joinToString(" ")
    }

    fun readVerdicts(verdicts: List<Verdict>, localeCode: String) {
        if (!ready) return
        val locale = when (localeCode) {
            "hi" -> Locale("hi", "IN")
            "te" -> Locale("te", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale.US
        }
        tts?.language = locale
        val text = verdicts.joinToString(". ") { "${it.title}. ${it.detail}" }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kosmos_verdicts")
    }
}
