package com.example.hammami.presentation.ui.features.appointments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.Error
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.Review
import com.example.hammami.domain.usecase.booking.CreateReviewUseCase
import com.example.hammami.domain.usecase.booking.GetUserBookingsFlowUseCase
import com.example.hammami.domain.usecase.user.GetCurrentUserFirstNameUseCase
import com.example.hammami.domain.usecase.validation.review.ValidateReviewRatingUseCase
import com.example.hammami.domain.usecase.validation.review.ValidateReviewTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getUserBookingsFlowUseCase: GetUserBookingsFlowUseCase,
    private val validateReviewTextUseCase: ValidateReviewTextUseCase,
    private val validateReviewRatingUseCase: ValidateReviewRatingUseCase,
    private val createReviewUseCase: CreateReviewUseCase,
    private val getCurrentUserFirstNameUseCase: GetCurrentUserFirstNameUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _newlyAddedReview = MutableStateFlow<Pair<String, Review>?>(null)
    val newlyAddedReview: StateFlow<Pair<String, Review>?> = _newlyAddedReview.asStateFlow()

    // StateFlow combinato che tiene traccia dello stato dell'UI
    val state: StateFlow<AppointmentsUiState> = getUserBookingsFlowUseCase(
    ).map { result ->
        Log.d("AppointmentsViewModel", "getCurrentUserIdResult: $result") // LOG
        when (result) {
            is Result.Success -> {
                // Dividi le prenotazioni in future e passate
                Log.d("AppointmentsViewModel", "Got userId: $result.data") // LOG
                val (pastBookings, futureBookings) = result.data.partition {
                    it.endDate < com.google.firebase.Timestamp.now()
                }
                Log.d("AppointmentsViewModel", "pastBookings: $pastBookings") // LOG
                Log.d("AppointmentsViewModel", "futureBookings: $futureBookings") // LOG
                AppointmentsUiState(
                    newAppointments = futureBookings,
                    pastAppointments = pastBookings,
                    isLoading = false
                )
            }

            is Result.Error -> {
                viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError(result.error.asUiText())) }
                AppointmentsUiState(isLoading = false) // Mantieni i dati precedenti
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Timeout di 5 secondi (opzionale)
        initialValue = AppointmentsUiState(isLoading = true) // Stato iniziale durante il caricamento
    )

    private val _state = MutableStateFlow(AppointmentsUiState())

    private fun emitUiEvent(event: UiEvent) {
        _state.update { it.copy(isLoading = false) }
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    fun setReviewAdded(serviceName: String, review: Review) {
        _newlyAddedReview.value = Pair(serviceName,review)
    }

    fun resetReviewAdded() {
        _newlyAddedReview.value = null
    }

    private fun validateSubmitFields(reviewText: String, rating: Float): Boolean {
        val textValidation = validateReviewTextUseCase(reviewText)
        val ratingValidation = validateReviewRatingUseCase(rating)

        val hasError = updateValidationErrors(textValidation, ratingValidation)
        return !hasError
    }

    private fun updateValidationErrors(
        textValidation: Result<Unit, Error>, ratingValidation: Result<Unit, Error>
    ): Boolean {
        val textError = (textValidation as? Result.Error)?.error?.asUiText()
        val ratingError = (ratingValidation as? Result.Error)?.error?.asUiText()

        _state.update {
            it.copy(
                textError = textError, ratingError = ratingError
            )
        }
        return textError != null || ratingError != null
    }


    suspend fun submitReview(
        reviewText: String, rating: Float, serviceName: String, booking: Booking
    ) {

        _state.update {
            it.copy(
                isLoading = false,
            )
        }

        Log.d("AppointmentsViewModel", "submitReview: Inizio") // Log di inizio
        Log.d("AppointmentsViewModel", "reviewText: $reviewText")
        Log.d("AppointmentsViewModel", "rating: $rating")
        Log.d("AppointmentsViewModel", "serviceName: $serviceName")
        Log.d("AppointmentsViewModel", "booking.id: ${booking.id}")

        Log.d("AppointmentsViewModel", "getCurrentUserFirstNameUseCase") // LOG

        when (val firstName = getCurrentUserFirstNameUseCase()) {
            is Result.Success -> {
                Log.d("AppointmentsViewModel", "firstName: ${firstName.data}") // LOG
                val userName = firstName.data
                Log.d("AppointmentsViewModel", "userName: $userName")
                createReview(reviewText, rating, serviceName, booking, userName)
            }

            is Result.Error -> {
                Log.e("AppointmentsViewModel", "Errore nel recuperare il nome utente", (firstName.error as Throwable))
                emitUiEvent(UiEvent.ShowError(firstName.error.asUiText()))
            }
        }
    }

    private suspend fun createReview(
        reviewText: String, rating: Float, serviceName: String, booking: Booking, userName: String
    ) {
        Log.d("AppointmentsViewModel", "createReview: $reviewText, $rating, $serviceName, $booking, $userName") // LOG
        when (val resultReview =
            createReviewUseCase(reviewText, rating, serviceName, booking, userName)) {
            is Result.Success -> {
                Log.d("AppointmentsViewModel", "createReview: ${resultReview.data}") // LOG
                _newlyAddedReview.value = Pair(serviceName, resultReview.data)
                emitUiEvent(UiEvent.ShowMessage(UiText.StringResource(R.string.review_added_successfully)))
            }

            is Result.Error -> {
                Log.e("AppointmentsViewModel", "createReview: ${resultReview.error}") // LOG
                emitUiEvent(UiEvent.ShowError(resultReview.error.asUiText()))
            }
        }
    }


    data class AppointmentsUiState(
        val newAppointments: List<Booking> = emptyList(),
        val pastAppointments: List<Booking> = emptyList(),
        val isLoading: Boolean = false,
        val reviewText: String = "",
        val rating: Float = 0f,
        val textError: UiText? = null,
        val ratingError: UiText? = null
    )

    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }
}