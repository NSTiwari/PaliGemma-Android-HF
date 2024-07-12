package com.example.paligemma.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CoordinatesModelViewModel(
    private val coordinatesModelUseCase: CoordinatesModelUseCase
) : ViewModel() {

    fun getCoordinatesModel() {
        try {
            coordinatesModelUseCase.invoke("")
            // prepare the data for ui layer
            // update ui state with data
        } catch (e: Exception) {
            // update  ui for error
        }
    }
}

class CoordinatesModelViewModelFactory(private val coordinatesModelUseCase: CoordinatesModelUseCase): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       return CoordinatesModelViewModel(coordinatesModelUseCase) as T
    }
}