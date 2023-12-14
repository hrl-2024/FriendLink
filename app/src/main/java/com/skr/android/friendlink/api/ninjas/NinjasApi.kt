package com.skr.android.friendlink.api.ninjas

import com.skr.android.friendlink.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = BuildConfig.NINJA_API_KEY

interface NinjasApi {
    @GET("quotes")
    suspend fun fetchQuote(
        @Query("category") category: String = "happiness",
        @Query("X-Api-Key") apikey: String = API_KEY
    ): List<Quote>
}