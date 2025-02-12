package com.example.hammami.presentation.ui.features.admin.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.result.Result
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.time.DateTimeUtils.toLocalDate
import com.example.hammami.core.time.DateTimeUtils.toLocalTime
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.usecase.booking.GetAvailableTimeSlotsUseCase
import com.example.hammami.domain.usecase.booking.GetBookingByIdUseCase
import com.example.hammami.domain.usecase.booking.ScheduleBookingReminderUseCase
import com.example.hammami.domain.usecase.booking.UpdateBookingDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditBookingViewModel @Inject constructor(
    private val getBookingByIdUseCase: GetBookingByIdUseCase,
    private val updateBookingDetailsUseCase: UpdateBookingDetailsUseCase,
    private val getAvailableTimeSlotsUseCase: GetAvailableTimeSlotsUseCase,
    private val scheduleBookingReminderUseCase: ScheduleBookingReminderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditBookingUiState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditBookingUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var serviceDurationMinutes: Int = 0


    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getBookingByIdUseCase(bookingId)) {
                is Result.Success -> handleBookingLoaded(result.data)
                is Result.Error -> emitUiEvent(EditBookingUiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private suspend fun handleBookingLoaded(booking: Booking) {
        val startDate = booking.startDate
        val endDate = booking.endDate

        val date = startDate.toLocalDate()
        val startTime = startDate.toLocalTime()
        val endTime = endDate.toLocalTime()
        serviceDurationMinutes = DateTimeUtils.calculateServiceDurationInMinutes(startTime, endTime)

        val availableTimeSlots = getAvailableTimeSlotsUseCase(
            date = date,
            serviceDurationMinutes = serviceDurationMinutes
        ).let { result ->
            if (result is Result.Success) result.data else emptyList()
        }

        _state.update {
            it.copy(
                booking = booking,
                isLoading = false,
                selectedDate = date,
                selectedTimeSlot = TimeSlot(startTime, endTime),
                availableTimeSlots = availableTimeSlots
            )
        }
    }

    fun onDateSelected(date: LocalDate) = viewModelScope.launch {
        _state.update { it.copy(selectedDate = date, availableTimeSlots = emptyList()) }
        loadAvailableTimeSlots(date)
    }

    private suspend fun loadAvailableTimeSlots(date: LocalDate) {
        _state.update { it.copy(isLoading = true) }
        when (val result = getAvailableTimeSlotsUseCase(date, serviceDurationMinutes)) {
            is Result.Success -> _state.update {
                it.copy(
                    isLoading = false,
                    availableTimeSlots = result.data
                )
            }

            is Result.Error -> emitUiEvent(EditBookingUiEvent.ShowError(result.error.asUiText()))
        }
    }

    fun onTimeSlotSelected(timeSlot: TimeSlot?) = viewModelScope.launch {
        _state.update { it.copy(selectedTimeSlot = timeSlot) }
        // Puoi aggiungere qui la logica per verificare la disponibilità, se necessario
    }


    fun onConfirmChanges() {
        viewModelScope.launch {
            val currentState = _state.value
            val bookingId = currentState.booking?.id ?: return@launch // Ottieni l'ID
            val selectedDate = currentState.selectedDate ?: return@launch
            val selectedTimeSlot = currentState.selectedTimeSlot ?: return@launch


            if (selectedTimeSlot.startTime >= selectedTimeSlot.endTime) {
                emitUiEvent(EditBookingUiEvent.ShowError(UiText.StringResource(R.string.error_invalid_time)))
                return@launch
            }

            val (startDate, endDate) = DateTimeUtils.createStartAndEndTimestamps(
                selectedDate,
                selectedTimeSlot.startTime,
                selectedTimeSlot.endTime
            )
            when (val result = updateBookingDetailsUseCase(
                bookingId = bookingId,
                startDate = startDate,
                endDate = endDate
            )) {
                is Result.Success ->
                {
                    when (val updatedBookingResult  = getBookingByIdUseCase(bookingId)) {
                        is Result.Success -> {
                            scheduleBookingReminderUseCase(updatedBookingResult .data)
                            emitUiEvent(EditBookingUiEvent.BookingUpdatedSuccessfully)
                        }
                        is Result.Error -> emitUiEvent(EditBookingUiEvent.ShowError(updatedBookingResult.error.asUiText()))
                    }
                    emitUiEvent(EditBookingUiEvent.BookingUpdatedSuccessfully)
                }
                is Result.Error -> emitUiEvent(EditBookingUiEvent.ShowError(result.error.asUiText()))
            }
        }
    }


    private suspend fun emitUiEvent(event: EditBookingUiEvent) {
        _state.update { it.copy(isLoading = false) }
        _uiEvent.emit(event)
    }

    data class EditBookingUiState(
        val booking: Booking? = null, // La prenotazione esistente
        val availableTimeSlots: List<TimeSlot> = emptyList(),
        val selectedDate: LocalDate? = null,
        val selectedTimeSlot: TimeSlot? = null,
        val isLoading: Boolean = false,
        // Aggiungi altri stati se necessario (es. per messaggi di errore)
    )

    sealed class EditBookingUiEvent {
        data class ShowError(val message: UiText) : EditBookingUiEvent()
        object BookingUpdatedSuccessfully : EditBookingUiEvent()
    }
}