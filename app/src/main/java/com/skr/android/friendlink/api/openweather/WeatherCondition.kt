package com.skr.android.friendlink.api.openweather

data class WeatherCondition(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)
