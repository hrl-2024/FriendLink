package com.skr.android.friendlink.ui.home.send

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.skr.android.friendlink.BuildConfig
import kotlin.time.Duration.Companion.seconds

private const val TAG = "PromptRepository"
private const val API_KEY = BuildConfig.OPENAI_API_KEY

class PromptRepository {
    val openAI = OpenAI(
        token = API_KEY,
        timeout = Timeout(socket = 60.seconds),
    )

    val chatCompletionRequest = ChatCompletionRequest(
        model = ModelId("gpt-3.5-turbo"),
        messages = listOf(
            ChatMessage(
                role = ChatRole.System,
                content = "You are a helpful assistant!"
            ),
            ChatMessage(
                role = ChatRole.User,
                content = "Hello!"
            )
        )
    )

    suspend fun fetchPrompt(): ChatCompletion {
        return openAI.chatCompletion(chatCompletionRequest)
    }
}