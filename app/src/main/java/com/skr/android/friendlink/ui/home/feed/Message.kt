package com.skr.android.friendlink.ui.home.feed

import java.util.Date

data class Message(
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: Date
)
