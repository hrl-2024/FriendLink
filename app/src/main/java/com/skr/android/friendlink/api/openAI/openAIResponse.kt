package com.skr.android.friendlink.api.openAI

import com.squareup.moshi.Json

data class OpenAIResponse(
    @Json(name = "choices") val choices: List<Choices>
)
