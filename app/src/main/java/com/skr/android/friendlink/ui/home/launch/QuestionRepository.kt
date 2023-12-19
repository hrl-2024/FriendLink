package com.skr.android.friendlink.ui.home.launch

import com.skr.android.friendlink.api.opentdb.OpenTDBApi
import com.skr.android.friendlink.api.opentdb.OpenTDBResponse
import com.skr.android.friendlink.api.opentdb.Question
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create


private const val BASE_URL = "https://opentdb.com/"

class QuestionRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    private val questionApi: OpenTDBApi = retrofit.create()

    suspend fun fetchQuestion(): String {
        val fetchedResponse = questionApi.fetchQuestions().results[0]

        return fetchedResponse.question
    }
}