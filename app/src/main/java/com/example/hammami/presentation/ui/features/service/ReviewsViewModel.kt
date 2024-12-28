package com.example.hammami.presentation.ui.features.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Review
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.GetReviewsUseCase
import com.example.hammami.core.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val getReviewsUseCase: GetReviewsUseCase
): ViewModel() {

    private val _allReviews = MutableStateFlow<List<Review>>(emptyList())
    val allReviews = _allReviews.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    /*
    init {
        //fetchAllBenessere()
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            loadBenessereData()
        }
    }
     */

    fun loadReviewsData(service: Service){
        viewModelScope.launch {
            when (val result = getReviewsUseCase(service.reviews)) {
                is Result.Success -> {
                    _allReviews.update { result.data }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

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