package com.skr.android.friendlink.api.openweather

import com.skr.android.friendlink.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = BuildConfig.OPEN_WEATHER_API_KEY

interface OpenWeatherApi {
    @GET("weather")
    suspend fun fetchWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): OpenWeatherResponse
}