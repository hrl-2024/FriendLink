package com.skr.android.friendlink.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Register Fragment"
    }
    val text: LiveData<String> = _text
}