package com.example.paligemma.data

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit


interface CoordinatesModelApi {

    @POST("/api/detect")
    @Multipart
    suspend fun getCoordinatesModel(
        @Part("prompt") text: RequestBody?,
        @Part("width") width: RequestBody?,
        @Part("height") height: RequestBody?,
        @Part("image\"; filename=\"image.jpg") image: RequestBody?,
    ): Response<CoordinatesModel>

    companion object {
        private val client: OkHttpClient =
            OkHttpClient
                .Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://paligemma.onrender.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(CoordinatesModelApi::class.java)
        }
    }
}
