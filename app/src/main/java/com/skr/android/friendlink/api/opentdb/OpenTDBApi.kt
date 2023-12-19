package com.skr.android.friendlink.api.opentdb

import retrofit2.http.GET

interface OpenTDBApi {

    @GET("api.php?amount=1&difficulty=easy&type=multiple")
    suspend fun fetchQuestions(): OpenTDBResponse
}