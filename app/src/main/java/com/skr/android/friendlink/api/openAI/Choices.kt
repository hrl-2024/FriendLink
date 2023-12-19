package com.skr.android.friendlink.api.openAI

data class Choices(
    val text: String,
    val index: Int,
    val logprobs: Long,
    val finish_reason: String
)