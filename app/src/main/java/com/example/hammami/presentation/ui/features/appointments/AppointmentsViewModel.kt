package com.example.hammami.presentation.ui.features.appointments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.core.time.DateTimeUtils.toEndOfDayTimestamp
import com.example.hammami.core.time.DateTimeUtils.toStartOfDayTimestamp
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.usecase.booking.GetUserBookingsFlowUseCase
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserBookingsFlowUseCase: GetUserBookingsFlowUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _today = MutableStateFlow(LocalDate.now())

    // StateFlow combinato che tiene traccia dello stato dell'UI
    val state: StateFlow<AppointmentsUiState> =
        getUserBookingsFlowUseCase(
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


    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    data class AppointmentsUiState(
        val newAppointments: List<Booking> = emptyList(),
        val pastAppointments: List<Booking> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }
}