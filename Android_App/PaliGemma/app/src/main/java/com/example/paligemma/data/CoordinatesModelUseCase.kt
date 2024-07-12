package com.example.paligemma.data

import retrofit2.Response

class CoordinatesModelUseCase(
    private val coordinatesModelRepo: CoordinatesModelRepo
) {

    operator fun invoke(text: String): Response<CoordinatesModel> {
        val data = coordinatesModelRepo.getCoordinatesModel(text)
        // perform operation on data, implement your business logics
        return data
    }
}