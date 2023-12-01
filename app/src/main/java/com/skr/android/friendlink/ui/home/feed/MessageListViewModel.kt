package com.skr.android.friendlink.ui.home.feed

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessageListViewModel : ViewModel() {

    private val _messages : MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())

    val messages : StateFlow<List<Message>> get() = _messages.asStateFlow()

    init {
        // collect all messages from the repository

        // Right now: some fake data
        _messages.value = listOf(
            Message("1", "Alice", "Hello", java.util.Date()),
            Message("2", "Bob", "Hi", java.util.Date()),
            Message("3", "Charlie", "Hey", java.util.Date())
        )
    }
}