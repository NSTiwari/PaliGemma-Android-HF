package com.example.paligemma.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CoordinatesModelApi {

    @POST("/api/detect")
    @Multipart
    suspend fun getCoordinatesModel(
        @Part("prompt") text: RequestBody?,
        @Part image: MultipartBody.Part?,
    ): Response<CoordinatesModel>

    companion object {
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://eefe-2409-40d6-1d-9d9c-69f7-3684-b625-23a.ngrok-free.app")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CoordinatesModelApi::class.java)
        }
    }
}