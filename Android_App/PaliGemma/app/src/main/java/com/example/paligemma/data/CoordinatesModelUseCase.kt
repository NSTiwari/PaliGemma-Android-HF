package com.example.paligemma.data

import android.net.Uri
import retrofit2.Response

class CoordinatesModelUseCase(
    private val coordinatesModelRepo: CoordinatesModelRepo
) {

    suspend operator fun invoke(requestModel: RequestModel): Response<CoordinatesModel> {
        return coordinatesModelRepo.getCoordinatesModel(requestModel)
    }
}