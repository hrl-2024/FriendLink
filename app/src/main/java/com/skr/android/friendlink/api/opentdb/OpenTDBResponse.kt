package com.skr.android.friendlink.api.opentdb

data class OpenTDBResponse(
    val response_code: Int,
    val results: List<Question>
)
