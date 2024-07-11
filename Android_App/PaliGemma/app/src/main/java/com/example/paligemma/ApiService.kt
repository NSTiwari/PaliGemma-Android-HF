package com.example.paligemma

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("your/api/endpoint") // Replace with your actual API endpoint
    suspend fun uploadImageWithText(
        @Part image: MultipartBody.Part,
        @Part("text") text: RequestBody
    ): Response<Any> // Adjust the response type as per your API
}