package com.example.paligemma.data

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CoordinatesModelViewModel(
    private val coordinatesModelRepo: CoordinatesModelRepo
) : ViewModel() {

    var coordinates = mutableStateOf<UiState>(UiState.Idle)

    fun getCoordinatesModel(requestModel: RequestModel) {
        coordinates.value = UiState.Loading
        viewModelScope.launch {
            try {
                val coordinatesModel = coordinatesModelRepo
                    .getCoordinatesModel(requestModel)
                    .body()
                if (coordinatesModel?.result != null) {
                    coordinates.value = UiState.Success(
                        coordinatesModel.result
                    )
                } else if (coordinatesModel?.error != null) {
                    coordinates.value = UiState.Error(
                        coordinatesModel.error
                    )
                } else {
                    coordinates.value = UiState.Error("No result found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message != null) {
                    coordinates.value = UiState.Error(e.message!!)
                } else {
                    coordinates.value = UiState.Error("An unknown error occurred.")
                }
            }
        }
    }

    fun resetData() {
        coordinates.value = UiState.Idle
    }
}

class CoordinatesModelViewModelFactory(private val coordinatesModelRepo: CoordinatesModelRepo) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CoordinatesModelViewModel(coordinatesModelRepo) as T
    }
}

sealed interface UiState {
    data object Idle : UiState
    data object Loading : UiState
    data class Success(val result: List<Result>) : UiState
    data class Error(val e: String) : UiState
}