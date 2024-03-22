package com.example.kdigitalaicompetition

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PredictService {
    @Multipart
    @POST("predict")
    fun uploadImage(
        @Part("question") question: RequestBody,
        @Part imageFile: MultipartBody.Part
    ): Call<ServerResponse>
}