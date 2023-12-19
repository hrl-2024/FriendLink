package com.skr.android.friendlink.api.openAI

import com.skr.android.friendlink.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = BuildConfig.OPENAI_API_KEY

interface OpenAIApi {
    @GET("completions")
    suspend fun fetchCompletion(
        @Query("Content-Type") type: String = "application/json",
        @Query("Authorization: Bearer") apikey: String = API_KEY,
        @Query("model") model: String = "gpt-3.5-turbo-instruct",
        @Query("prompt") prompt: String = "Say this is a test",
        @Query("max_tokens") maxTokens: Int = 1,
        @Query("temperature") temperature: Double = 0.0
    ): OpenAIResponse
}