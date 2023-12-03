package com.skr.android.friendlink.ui.home.feed

import java.util.Date

data class Message(
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long
)
