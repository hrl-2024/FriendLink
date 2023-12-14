package com.skr.android.friendlink.ui.home.send

import com.skr.android.friendlink.api.ninjas.NinjasApi
import com.skr.android.friendlink.api.ninjas.Quote
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create


private const val BASE_URL = "https://api.api-ninjas.com/v1/"

class QuoteRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    private val ninjasApi: NinjasApi = retrofit.create()

    suspend fun fetchQuote(): Quote {
        val fetchedResponse: List<Quote> = ninjasApi.fetchQuote(category = "happiness")

        return fetchedResponse[0]
    }
}