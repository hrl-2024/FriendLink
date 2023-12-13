package com.skr.android.friendlink.api.openweather

import com.squareup.moshi.Json

data class OpenWeatherResponse(
    @Json(name = "weather") val weather: List<WeatherCondition>,
    @Json(name = "main") val main: WeatherDetail
)
