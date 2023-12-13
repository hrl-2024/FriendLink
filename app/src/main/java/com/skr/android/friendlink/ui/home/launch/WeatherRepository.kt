package com.skr.android.friendlink.ui.home.launch

import com.skr.android.friendlink.api.openweather.OpenWeatherApi
import com.skr.android.friendlink.api.openweather.OpenWeatherResponse
import com.skr.android.friendlink.api.openweather.WeatherInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create


private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

class WeatherRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    private val openWeatherApi: OpenWeatherApi = retrofit.create()

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo {
        val fetchedResponse: OpenWeatherResponse = openWeatherApi.fetchWeather(lat, lon)

        return WeatherInfo(
            description = fetchedResponse.weather[0].description,
            icon = fetchedResponse.weather[0].icon,
            id = fetchedResponse.weather[0].id,
            main = fetchedResponse.weather[0].main,
            temp = fetchedResponse.main.temp,
        )
    }
}