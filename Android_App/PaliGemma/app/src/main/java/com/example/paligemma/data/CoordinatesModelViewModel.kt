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
                coordinates.value = UiState.Success(
                    coordinatesModelRepo.getCoordinatesModel(
                        requestModel
                    ).body()
                )
                // prepare the data for ui layer
                // update ui state with data
            } catch (e: Exception) {
                e.printStackTrace()
                // update  ui for error
                coordinates.value = UiState.Error(e)
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
    data class Success(val coordinatesModel: CoordinatesModel?) : UiState
    data class Error(val e: Exception) : UiState
}