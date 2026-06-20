package com.kosmos.android.data

import android.content.Context
import com.kosmos.shared.api.createHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ChatRepository(private val context: Context) {

    private val client = createHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs = PreferencesRepository(context)

    sealed class ChatResult {
        data class Success(val answer: String) : ChatResult()
        data object LimitReached : ChatResult()
        data class Error(val message: String) : ChatResult()
    }

    suspend fun ask(question: String, weatherContext: String, localeCode: String = "en"): ChatResult {
        if (!prefs.consumeChatMessage()) {
            return ChatResult.LimitReached
        }

        val locale = com.kosmos.shared.i18n.AppLocale.fromCode(localeCode)
        val languageRule = com.kosmos.shared.i18n.LocaleStrings.chatLanguageInstruction(locale)

        return try {
            val response = client.post("https://text.pollinations.ai/openai") {
                contentType(ContentType.Application.Json)
                setBody(
                    ChatRequest(
                        model = "openai",
                        messages = listOf(
                            ChatMessage(
                                role = "system",
                                content = """
                                    You are Kosmos, a weather + wellness companion for India.
                                    Decision-first. Specific times. Max 2 emojis. Never start with Hello or Sure.
                                    Never show raw numbers without plain English meaning.
                                    $languageRule
                                    Current weather context: $weatherContext
                                """.trimIndent(),
                            ),
                            ChatMessage(role = "user", content = question),
                        ),
                    ),
                )
            }.body<String>()

            val answer = json.decodeFromString<ChatResponse>(response)
                .choices.firstOrNull()?.message?.content
                ?: "Couldn't get an answer right now — try again in a moment."
            ChatResult.Success(answer)
        } catch (_: Exception) {
            ChatResult.Error("Couldn't reach Kosmos right now — check your connection and try again.")
        }
    }

    suspend fun messagesRemaining(): Int = prefs.getChatMessagesRemaining()

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
    )

    @Serializable
    private data class ChatMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class ChatResponse(
        val choices: List<Choice>,
    ) {
        @Serializable
        data class Choice(val message: ChatMessage)
    }
}
