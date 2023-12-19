package com.skr.android.friendlink.ui.home.launch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skr.android.friendlink.api.openweather.WeatherInfo
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(private val lat: Double, private val lon: Double) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _weatherInfo = MutableLiveData<WeatherInfo>()
    val weatherInfo: LiveData<WeatherInfo> get() = _weatherInfo

    private val _question = MutableLiveData<String>()
    val question: LiveData<String> get() = _question

    init {
        // default location is Mountain View, CA
        viewModelScope.launch {
            try {
                val weather = WeatherRepository().fetchWeather(lat, lon)
                _weatherInfo.value = weather
                Log.d(TAG, "Weather received: $weather")

                val question = QuestionRepository().fetchQuestion()
                _question.value = question
                Log.d(TAG, "Question received: $question")
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch weather/question", ex)
            }
        }
    }
}