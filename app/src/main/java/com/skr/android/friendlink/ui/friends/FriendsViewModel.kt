package com.skr.android.friendlink.ui.friends

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FriendsViewModel : ViewModel() {

    private val _friends : MutableStateFlow<List<Friend>> = MutableStateFlow(emptyList())

    val friends : StateFlow<List<Friend>> get() = _friends.asStateFlow()

    init {
        // collect all messages from the repository

        // Right now: some fake data
        for (i in 1..100) {
            _friends.value += Friend(i.toString(), "", "Alice", "", "123-345-6789")
        }
    }
}