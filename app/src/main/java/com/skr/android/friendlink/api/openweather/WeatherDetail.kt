package com.skr.android.friendlink.api.openweather

data class WeatherDetail(
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)
