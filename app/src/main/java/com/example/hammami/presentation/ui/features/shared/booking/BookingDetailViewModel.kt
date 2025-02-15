package com.example.hammami.presentation.ui.features.shared.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingOption
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.booking.CancelBookingUseCase
import com.example.hammami.domain.usecase.booking.GetBookingByIdUseCase
import com.example.hammami.domain.usecase.booking.SendCancellationNotificationUseCase
import com.example.hammami.domain.usecase.user.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val getBookingByIdUseCase: GetBookingByIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val sendCancellationNotificationUseCase: SendCancellationNotificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookingDetailUiState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<BookingDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    var isAdmin: Boolean = false

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
                is Result.Success -> {
                    isAdmin = result.data.isadmin
                    _state.update { it.copy(user = result.data) }
                    updateAvailableOptions()
                }
                is Result.Error -> {
                    emitUiEvent(BookingDetailUiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    private fun updateAvailableOptions() {
        val options = mutableListOf(
            BookingOption(
                title = "Modifica Prenotazione",
                iconResId = R.drawable.ic_schedule,
                type = OptionType.EDIT
            ),
            BookingOption(
                title = "Annulla Prenotazione",
                iconResId = R.drawable.ic_delete,
                type = OptionType.CANCEL
            )
        )
        if (!isAdmin) {
            options.add(
                BookingOption(
                    title = "Avvia Navigazione",
                    iconResId = R.drawable.ic_navigation,
                    type = OptionType.NAVIGATE
                )
            )
        }
        _state.update { it.copy(availableOptions = options, isNavigationOptionVisible = !isAdmin) }
    }

    fun onOptionSelected(optionType: OptionType) {
        viewModelScope.launch {
            when (optionType) {
              OptionType.EDIT -> {
                    _uiEvent.send(BookingDetailUiEvent.NavigateToEditBooking(_state.value.booking?.id!!))
                }
                OptionType.CANCEL -> {
                    _state.value.booking?.id?.let {
                        _uiEvent.send(BookingDetailUiEvent.ConfirmCancellation(it))
                    }
                }
       OptionType.NAVIGATE -> {
                    _uiEvent.send(BookingDetailUiEvent.StartNavigation)
                }
            }
        }
    }


    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            when (val result = cancelBookingUseCase(bookingId)) {
                is Result.Success -> {
                    // Invia la notifica di cancellazione *immediatamente*
                    sendCancellationNotificationUseCase(bookingId)
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
        _uiEvent.send(event)
    }

    data class BookingDetailUiState(
        val booking: Booking? = null,
        val user: User? = null,
        val isLoading: Boolean = false,
        val availableOptions: List<BookingOption> = emptyList(),
        val isNavigationOptionVisible: Boolean = false
    )

    sealed class BookingDetailUiEvent {
        data class ShowError(val message: UiText) : BookingDetailUiEvent()
        object BookingCancelled: BookingDetailUiEvent()
        data class NavigateToEditBooking(val bookingId: String) : BookingDetailUiEvent()
        object StartNavigation : BookingDetailUiEvent()
        data class ConfirmCancellation(val bookingId: String) : BookingDetailUiEvent()
    }

    enum class OptionType {
        EDIT,
        CANCEL,
        NAVIGATE
    }
}