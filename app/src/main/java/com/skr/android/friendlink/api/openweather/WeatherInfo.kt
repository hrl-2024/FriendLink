package com.skr.android.friendlink.api.openweather

data class WeatherInfo(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String,
    val temp: Double,
)
