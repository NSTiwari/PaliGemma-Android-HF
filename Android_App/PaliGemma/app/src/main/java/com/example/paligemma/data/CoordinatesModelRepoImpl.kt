package com.example.paligemma.data

import retrofit2.Response

class CoordinatesModelRepoImpl(
    private val coordinatesModelApi: CoordinatesModelApi
) : CoordinatesModelRepo {


    override fun getCoordinatesModel(text: String): Response<CoordinatesModel> {
        return coordinatesModelApi.getCoordinatesModel(
            null, null
        )
    }

}