package com.example.hammami.presentation.ui.features.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.TimeSlotCalculator
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.usecase.booking.CancelBookingUseCase
import com.example.hammami.domain.usecase.booking.CreateBookingUseCase
import com.example.hammami.domain.usecase.booking.GetAvailableTimeSlotsUseCase
import com.example.hammami.domain.usecase.booking.GetBookingByIdUseCase
import com.example.hammami.domain.usecase.booking.IsTimeSlotAvailableUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

 @AssistedFactory
interface BookingViewModelFactory {
    fun create(service: Service): BookingViewModel
}

class BookingViewModel @AssistedInject constructor(
    @Assisted private val service: Service,
    private val getAvailableTimeSlotsUseCase: GetAvailableTimeSlotsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val getBookingUseCase: GetBookingByIdUseCase,
    private val isTimeSlotAvailableUseCase: IsTimeSlotAvailableUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState(service = service))
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BookingUiEvent>()
    val uiEvent: SharedFlow<BookingUiEvent> = _uiEvent.asSharedFlow()

    private val _newBooking = MutableStateFlow<Booking?>(null)
    val newBooking = _newBooking.asStateFlow()

    private var reservationJob: Job? = null
    private val reservationDuration = 5.minutes

    private var releaseSlotTimerJob: Job? = null
    private val slotReservationTimeout = 5.minutes // Imposta la durata del timer (es. 5 minuti)

    fun loadBooking(bookingId: String) = viewModelScope.launch {
        when (val result = getBookingUseCase(bookingId)) {
            is Result.Success -> {
                _newBooking.value = result.data
                _uiState.update { it.copy(isLoading = false) }
            }

            is Result.Error -> _uiEvent.emit(BookingUiEvent.ShowError(result.error.asUiText()))
        }
    }

    fun onServiceChanged(service: Service) {
        _uiState.update { it.copy(service = service) }
    }

    fun onDateSelected(dateString: String) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        val date = LocalDate.parse(dateString, dateFormatter)

        _uiState.update { it.copy(selectedDate = date, availableTimeSlots = emptyList()) }
        viewModelScope.launch {
            if (date != null) {
                loadAvailableTimeSlots(date)
            }
        }
    }

    fun resetBookingConfirmation() {
        _uiState.update { it.copy(isBookingConfirmed = false) }
    }

    fun onTimeSlotSelected(timeSlot: TimeSlotCalculator.AvailableSlot?) = viewModelScope.launch {
        _uiState.update { it.copy(selectedTimeSlot = timeSlot) }
    }

    suspend fun onConfirmBooking() {
        val state = uiState.value
        val selectedDate = state.selectedDate ?: return
        val selectedTimeSlot = state.selectedTimeSlot ?: return

        when (val availabilityResult = isTimeSlotAvailableUseCase(selectedDate, selectedTimeSlot)) {
            is Result.Success -> {
                if (availabilityResult.data) {
                    // Lo slot è disponibile, procedi con la prenotazione
                    reserveSlot()
                    val service = uiState.value.service ?: return
                    val currentBookingId = uiState.value.currentBookingId

                    val paymentItem = PaymentItem.ServiceBookingPayment.from(
                        service = state.service!!,
                        bookingId = currentBookingId ?: "",
                        date = selectedDate,
                        startTime = selectedTimeSlot.startTime,
                        endTime = selectedTimeSlot.endTime
                    )
                    _uiEvent.emit(BookingUiEvent.NavigateToPayment(paymentItem))
                } else {
                    // Lo slot non è più disponibile
                    _uiEvent.emit(
                        BookingUiEvent.ShowError(
                            UiText.DynamicString("L'orario selezionato non è più disponibile. Si prega di selezionarne un altro.")
                        )
                    )
                }
            }
            is Result.Error -> {
                // Gestisci l'errore nel recupero della disponibilità
                _uiEvent.emit(BookingUiEvent.ShowError(availabilityResult.error.asUiText()))
            }
        }
    }
    private suspend fun reserveSlot() {
        val service = uiState.value.service ?: return
        val selectedDate = uiState.value.selectedDate ?: return
        val selectedTimeSlot = uiState.value.selectedTimeSlot ?: return

        releaseSlotTimerJob?.cancel() // Annulla il timer precedente, se presente

        when (val result = createBookingUseCase(
            service = service,
            selectedDate = selectedDate,
            startTime = selectedTimeSlot.startTime,
            endTime = selectedTimeSlot.endTime,
            status = BookingStatus.RESERVED
        )) {
            is Result.Success -> {
                Log.d("BookingViewModel", "currentBookingId: ${result.data.id}")
                _uiState.update {
                    it.copy(
                        currentBookingId = result.data.id,
                        isSlotReserved = true,
                        reservationTimerActive = true,
                        availableTimeSlots = emptyList(),
                    )
                }
                _newBooking.value = result.data
                startReservationTimer()
                //Avvia il timer di rilascio
                releaseSlotTimerJob = viewModelScope.launch {
                    delay(slotReservationTimeout)
                    releaseReservedSlot()
                    _uiEvent.emit(BookingUiEvent.ShowError(UiText.DynamicString("Tempo scaduto. Rieffettua la prenotazione")))
                }
            }

            is Result.Error -> {
                _uiState.update { it.copy(isSlotReserved = false, reservationTimerActive = false) }
                _uiEvent.emit(BookingUiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private fun startReservationTimer() {
        reservationJob?.cancel() // cancello il job precedente se esiste
        reservationJob = viewModelScope.launch {
            _uiState.update { it.copy(isSlotReserved = true, reservationTimerActive = true) }
            delay(reservationDuration)
            releaseReservedSlot() // rilascio lo slot riservato dopo il ritardo
            _uiEvent.emit(BookingUiEvent.ShowError(UiText.DynamicString("Tempo scaduto. Rieffettua la prenotazione")))
        }
    }

    private suspend fun releaseReservedSlot() {
        val currentBookingId = uiState.value.currentBookingId ?: return
        _uiState.update { it.copy(isSlotReserved = false, reservationTimerActive = false) }
        when (val result = cancelBookingUseCase(currentBookingId)) {
            is Result.Success -> _uiState.update {
                it.copy(
                    currentBookingId = null,
                )
            }

            is Result.Error -> _uiEvent.emit(BookingUiEvent.ShowError(result.error.asUiText()))
        }
    }

    private suspend fun loadAvailableTimeSlots(date: LocalDate) {
        val service = uiState.value.service ?: return
        _uiState.update { it.copy(isLoading = true) }

        when (val result = getAvailableTimeSlotsUseCase(
            date = date,
            serviceDurationMinutes = service.length?.toInt() ?: 60
        )) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false, availableTimeSlots = result.data
                    )
                }
            }

            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false, availableTimeSlots = emptyList()
                    )
                }
                _uiEvent.emit(BookingUiEvent.ShowError(result.error.asUiText()))
            }
        }

    }

    data class BookingUiState(
        val service: Service? = null,
        val currentBookingId: String? = null,
        val availableTimeSlots: List<TimeSlotCalculator.AvailableSlot> = emptyList(),
        val selectedDate: LocalDate? = null,
        val selectedTimeSlot: TimeSlotCalculator.AvailableSlot? = null,
        val isBookingConfirmed: Boolean = false,
        val isSlotReserved: Boolean = false,
        val reservationTimerActive: Boolean = false,
        val isLoading: Boolean = false,
    )

    sealed interface BookingUiEvent {
        data class ShowUserMassage(val message: UiText) : BookingUiEvent
        data class ShowError(val message: UiText) : BookingUiEvent
        data class NavigateToPayment(val paymentItem: PaymentItem.ServiceBookingPayment) :
            BookingUiEvent
    }
}