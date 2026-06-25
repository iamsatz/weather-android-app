package com.kosmos.android.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.data.ChatRepository
import com.kosmos.android.data.PreferencesRepository
import com.kosmos.android.service.VoiceInputHelper
import com.kosmos.android.ui.designsystem.atoms.KplusBadge
import com.kosmos.android.ui.designsystem.atoms.SuggestionChip
import com.kosmos.android.ui.designsystem.atoms.kosmosGlossyGlass
import com.kosmos.android.ui.designsystem.tokens.KosmosColor
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val content: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSheet(
    weatherContext: String,
    chatRemaining: Int,
    localeCode: String = "en",
    isKosmosPlus: Boolean = false,
    onDismiss: () -> Unit,
    onRemainingChanged: (Int) -> Unit,
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val chatRepo = remember { ChatRepository(context.applicationContext) }
    val listState = rememberLazyListState()
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "assistant",
                content = "Ask me about your day — rain, walks, heat, air quality, anything weather-related.",
            ),
        )
    }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val limitReached = !isKosmosPlus && chatRemaining <= 0
    val voiceHelper = remember { (context as? android.app.Activity)?.let { VoiceInputHelper(it) } }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { voiceHelper?.destroy() }
    }

    val suggestions = listOf(
        "Should I run at 6 PM?",
        "Is it safe for kids outside?",
        "When's the Vit D window tomorrow?",
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ask Komos", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        if (limitReached) {
                            KplusBadge(modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Text(
                        text = if (limitReached) {
                            "Daily limit reached · Komos+ for unlimited"
                        } else if (isKosmosPlus) {
                            "Unlimited questions with Komos+"
                        } else {
                            "$chatRemaining of ${PreferencesRepository.FREE_CHAT_DAILY} free questions left today"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        text = suggestion,
                        enabled = !isLoading && !limitReached,
                        onClick = {
                            scope.launch {
                                sendMessage(
                                    suggestion,
                                    chatRepo,
                                    weatherContext,
                                    localeCode,
                                    messages,
                                    onRemainingChanged,
                                ) { isLoading = it }
                            }
                        },
                    )
                }
            }

            ChatInputPill(
                value = input,
                onValueChange = { input = it },
                enabled = !isLoading && !limitReached,
                limitReached = limitReached,
                onMicClick = {
                    voiceHelper?.startListening(
                        localeCode = localeCode,
                        onResult = { spoken -> input = spoken },
                        onError = {},
                    )
                },
                showMic = voiceHelper != null,
                onSend = {
                    if (input.isNotBlank() && !isLoading && !limitReached) {
                        val question = input
                        input = ""
                        scope.launch {
                            sendMessage(
                                question,
                                chatRepo,
                                weatherContext,
                                localeCode,
                                messages,
                                onRemainingChanged,
                            ) { isLoading = it }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun ChatInputPill(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    limitReached: Boolean,
    showMic: Boolean,
    onMicClick: () -> Unit,
    onSend: () -> Unit,
) {
    val colors = KosmosThemeExt.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .kosmosGlossyGlass()
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showMic) {
            IconButton(onClick = onMicClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Mic, contentDescription = "Voice input", tint = KosmosColor.primary)
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontSize = 15.sp,
            ),
            cursorBrush = SolidColor(KosmosColor.primary),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = if (limitReached) "Come back tomorrow or get Komos+" else "Ask about your day…",
                        color = colors.textMuted,
                        fontSize = 15.sp,
                    )
                }
                inner()
            },
            singleLine = true,
        )
        IconButton(
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (enabled && value.isNotBlank()) KosmosColor.primary
                    else KosmosColor.primary.copy(alpha = 0.35f),
                ),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private suspend fun sendMessage(
    question: String,
    chatRepo: ChatRepository,
    weatherContext: String,
    localeCode: String,
    messages: MutableList<ChatMessage>,
    onRemainingChanged: (Int) -> Unit,
    setLoading: (Boolean) -> Unit,
) {
    messages.add(ChatMessage("user", question))
    setLoading(true)
    when (val result = chatRepo.ask(question, weatherContext, localeCode)) {
        is ChatRepository.ChatResult.Success ->
            messages.add(ChatMessage("assistant", result.answer))
        is ChatRepository.ChatResult.LimitReached ->
            messages.add(ChatMessage("assistant", "You've used your 5 free questions today. Komos+ unlocks unlimited chat."))
        is ChatRepository.ChatResult.Error ->
            messages.add(ChatMessage("assistant", result.message))
    }
    onRemainingChanged(chatRepo.messagesRemaining())
    setLoading(false)
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (isUser) KosmosColor.primary else KosmosThemeExt.colors.cardBackground
    val textColor = if (isUser) Color.White else KosmosThemeExt.colors.textPrimary
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp,
    )

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Text(
            text = message.content,
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.78f else 0.92f)
                .clip(shape)
                .background(bg)
                .then(
                    if (!isUser) {
                        Modifier.border(1.dp, KosmosThemeExt.colors.border, shape)
                    } else Modifier,
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
            color = textColor,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}
