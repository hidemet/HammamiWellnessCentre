package com.example.hammami.presentation.ui.features.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.GetReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val getReviewsUseCase: GetReviewsUseCase,
    //private val serviceSelected: Service
): ViewModel() {

    //private val _allReviews= MutableStateFlow(ServiceReviews())
    //val allReviews = _allReviews.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    /*
    init {
        loadData(serviceSelected)
    }
     */

    private var service: Service? = null

    fun setService(service: Service) {
        this.service = service
        //loadData()
    }

    fun getService(): Service? {
        return service
    }


    /*
    private fun loadData() {
        service?.let {
            viewModelScope.launch {
                loadReviewsData(it)
            }
        }
    }

    private suspend fun loadReviewsData(service: Service){
        val allReviews = mutableListOf<Review>()
        when (val result = getReviewsUseCase(service.reviews)) {
            is Result.Success -> {
                allReviews.addAll(result.data)
            }
            is Result.Error -> {
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
            }
        }
        _allReviews.update { it.copy(serviceReviews = allReviews) }
    }

    data class ServiceReviews(
        val serviceReviews: List<Review> = emptyList()
    )

     */

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }

}