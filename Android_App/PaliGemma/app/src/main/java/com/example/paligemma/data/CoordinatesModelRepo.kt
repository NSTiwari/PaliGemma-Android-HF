package com.example.paligemma.data

import retrofit2.Response

interface CoordinatesModelRepo {
    fun getCoordinatesModel(text: String): Response<CoordinatesModel>
}