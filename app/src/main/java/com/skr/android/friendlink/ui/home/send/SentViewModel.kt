package com.skr.android.friendlink.ui.home.send

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skr.android.friendlink.api.ninjas.Quote
import kotlinx.coroutines.launch

private const val TAG = "SentViewModel"
const val QUOTE_KEY = "QUOTE_KEY"

class SentViewModel() : ViewModel() {

    private val _quote = MutableLiveData<Quote>().apply { value = Quote("", "", "") }
    var quote: LiveData<Quote> = _quote

    init {
        viewModelScope.launch {
            try {
                val quote = QuoteRepository().fetchQuote()
                _quote.value = quote
                Log.d(TAG, "Quote received: $quote")
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch quote", ex)
            }
        }
    }
}