package com.example.hammami.presentation.ui.features.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.Review
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.AddReviewToServiceUseCase
import com.example.hammami.domain.usecase.GetCurrentUserDataUseCase
import com.example.hammami.domain.usecase.GetIdFromNameUseCase
import com.example.hammami.domain.usecase.SetReviewUseCase
import com.example.hammami.domain.usecase.booking.UpdateBookingReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddReviewViewModel @Inject constructor(
    private val setReviewUseCase: SetReviewUseCase,
    private val addReviewToServiceUseCase: AddReviewToServiceUseCase,
    private val getIdFromNameUseCase: GetIdFromNameUseCase,
    private val updateBookingReviewUseCase: UpdateBookingReviewUseCase,
    private val getCurrentUserDataUseCase: GetCurrentUserDataUseCase

) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddReviewUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    fun submitReview(review: Review, serviceName: String, booking: Booking) {
        viewModelScope.launch {
            if (review.commento.isEmpty()) {
                _uiEvent.emit(AddReviewUiEvent.ShowError(UiText.DynamicString("La recensione non può essere vuota")))
                return@launch
            }

            if (review.valutazione == 0.0f) {
                _uiEvent.emit(AddReviewUiEvent.ShowError(UiText.DynamicString("Inserisci una valutazione")))
                return@launch
            }

            when (val result = setReviewUseCase(review)) {
                is Result.Success -> {
                    val reviewId = result.data.first  // Ottieni il percorso della recensione
                    when (val result2 = getIdFromNameUseCase(serviceName)) {
                        is Result.Success -> {
                            val servicePath = result2.data ?: ""
                            addReviewToServiceUseCase(
                                servicePath,
                                reviewId
                            ) // Collega recensione al servizio
                            updateBookingReviewUseCase(booking.id) // Imposta hasReview a true
                            _uiEvent.emit(
                                AddReviewUiEvent.ReviewAddedSuccessfully(
                                    serviceName,
                                    review
                                )
                            )
                        }

                        is Result.Error -> {
                            _uiEvent.emit(AddReviewUiEvent.ShowError(result2.error.asUiText()))
                        }
                    }
                }
                is Result.Error -> {
                    _uiEvent.emit(AddReviewUiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            when (val result = getCurrentUserDataUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(user = result.data) }
                }

                is Result.Error -> {
                    _uiEvent.emit(AddReviewUiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }


    data class AddReviewUiState(
        val user: User? = null,
        val isLoading: Boolean = false,
        val reviewText: String = "",
        val rating: Float = 0f
    )

    sealed class AddReviewUiEvent {
        data class ShowError(val message: UiText) : AddReviewUiEvent()
        data class ReviewAddedSuccessfully(val serviceName: String, val review: Review) :
            AddReviewUiEvent()
    }

}