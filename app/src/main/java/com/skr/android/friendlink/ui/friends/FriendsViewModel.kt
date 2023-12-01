package com.skr.android.friendlink.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FriendsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "CONTACT"
    }
    val text: LiveData<String> = _text
}