package com.example.paligemma.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Part

interface CoordinatesModelApi {
    
    @GET("your endpoint here")
    fun getCoordinatesModel(
        @Part image: MultipartBody.Part?,
        @Part("text") text: RequestBody?
    ): Response<CoordinatesModel>
}