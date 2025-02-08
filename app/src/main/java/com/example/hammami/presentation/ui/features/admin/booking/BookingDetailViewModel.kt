package com.example.hammami.presentation.ui.features.admin.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.booking.CancelBookingUseCase
import com.example.hammami.domain.usecase.booking.GetBookingByIdUseCase
import com.example.hammami.domain.usecase.user.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val getBookingByIdUseCase: GetBookingByIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookingDetailUiState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BookingDetailUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getBookingByIdUseCase(bookingId)) {
                is Result.Success -> {
                    _state.update { it.copy(booking = result.data, isLoading = false) }
                    loadUser(result.data.userId)
                }
                is Result.Error -> emitUiEvent(BookingDetailUiEvent.ShowError(result.error.asUiText()))

            }
        }
    }

    private fun loadUser(userId: String) {
        viewModelScope.launch {
            when (val result = getUserByIdUseCase(userId)) {
                is Result.Success -> _state.update { it.copy(user = result.data) }

                is Result.Error -> {
                    emitUiEvent(BookingDetailUiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            when(val result = cancelBookingUseCase(bookingId)) {
                is Result.Success -> {
                    emitUiEvent(BookingDetailUiEvent.BookingCancelled)
                }
                is Result.Error -> {
                    emitUiEvent(BookingDetailUiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    private suspend fun emitUiEvent(event: BookingDetailUiEvent) {
        _state.update { it.copy(isLoading = false) }
        _uiEvent.emit(event)
    }

    data class BookingDetailUiState(
        val booking: Booking? = null,
        val user: User? = null,
        val isLoading: Boolean = false,
    )

    sealed class BookingDetailUiEvent {
        data class ShowError(val message: UiText) : BookingDetailUiEvent()
        object BookingCancelled: BookingDetailUiEvent() //evento per notificare che la prenotazione è stata cancellata
    }
}