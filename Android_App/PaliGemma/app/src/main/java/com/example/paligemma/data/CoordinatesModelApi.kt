package com.example.paligemma.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part

interface CoordinatesModelApi {

    @GET("api/detect")
    @Multipart
    suspend fun getCoordinatesModel(
        @Part image: MultipartBody.Part?,
        @Part("text") text: String?
    ): Response<CoordinatesModel>

    companion object {
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://127.0.0.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CoordinatesModelApi::class.java)
        }
    }
}