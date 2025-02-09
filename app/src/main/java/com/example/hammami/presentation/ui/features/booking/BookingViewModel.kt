package com.example.hammami.presentation.ui.features.booking


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.result.Result
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.core.ui.UiText
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes

@AssistedFactory
interface BookingViewModelFactory {
    fun create(service: Service): BookingViewModel
}

class BookingViewModel @AssistedInject constructor(
    @Assisted private var service: Service,
    private val getAvailableTimeSlotsUseCase: GetAvailableTimeSlotsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val getBookingUseCase: GetBookingByIdUseCase,
    private val isTimeSlotAvailableUseCase: IsTimeSlotAvailableUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState(service = service))
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<BookingUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _newBooking = MutableStateFlow<Booking?>(null)
    val newBooking = _newBooking.asStateFlow()

    private var serviceDurationMinutes: Int = 0

    private var reservationJob: Job? = null
    private val reservationDuration = 5.minutes

    private var releaseSlotTimerJob: Job? = null
    private val slotReservationTimeout = 5.minutes


    init {
        service.length?.toInt()?.let {
            serviceDurationMinutes = it
        }
    }

    fun loadBooking(bookingId: String) = viewModelScope.launch {
        when (val result = getBookingUseCase(bookingId)) {
            is Result.Success -> {
                _newBooking.value = result.data
                _uiState.update { it.copy(isLoading = false) }
            }

            is Result.Error -> _uiEvent.send(BookingUiEvent.ShowError(result.error.asUiText()))
        }
    }

    fun setService(newService: Service) {
        if (service != newService) {
            service = newService
            _uiState.value = BookingUiState(service = service)

            viewModelScope.launch {
                uiState.value.selectedDate?.let { loadAvailableTimeSlots(it) }
            }
        }
    }

    fun resetState() {
        _uiState.value = BookingUiState(service = service) // Reimposta allo stato iniziale
    }

    fun onServiceChanged(service: Service) {
        _uiState.update { it.copy(service = service) }
    }

    fun onDateSelected(date: LocalDate) = viewModelScope.launch {
        val dayOfWeek = date.dayOfWeek.value
        val isClosed = (dayOfWeek == 7 || dayOfWeek == 1)

        _uiState.update {
            it.copy(
                selectedDate = date,
                availableTimeSlots = emptyList(),
                selectedTimeSlot = null,
                isClosedDay = isClosed
            )
        }

        if (!isClosed) {
            loadAvailableTimeSlots(date)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }


    fun resetBookingConfirmation() {
        _uiState.update { it.copy(isBookingConfirmed = false) }
    }

    fun onTimeSlotSelected(timeSlot: TimeSlot?) = viewModelScope.launch {
        _uiState.update { it.copy(selectedTimeSlot = timeSlot) }
    }

    fun onTimeSlotSelected(startTime: LocalTime) {
        val selectedSlot = uiState.value.availableTimeSlots.find { it.startTime == startTime }
        _uiState.update { it.copy(selectedTimeSlot = selectedSlot) }

    }


    suspend fun onConfirmBooking() {
        val state = uiState.value
        val selectedDate = state.selectedDate ?: return
        val selectedTimeSlot = state.selectedTimeSlot ?: return

        when (val availabilityResult = isTimeSlotAvailableUseCase(selectedDate, selectedTimeSlot)) {
            is Result.Success -> {
                if (availabilityResult.data) {
                    reserveSlot()

                } else {
                    _uiEvent.send(
                        BookingUiEvent.ShowError(
                            UiText.DynamicString("L'orario selezionato non è più disponibile. Si prega di selezionarne un altro.")
                        )
                    )
                }
            }

            is Result.Error -> {
                _uiEvent.send(BookingUiEvent.ShowError(availabilityResult.error.asUiText()))
            }
        }
    }

    private suspend fun reserveSlot() {
        val state = uiState.value
        val selectedDate = state.selectedDate ?: return
        val selectedTimeSlot = uiState.value.selectedTimeSlot ?: return
        val price = service.price?.toDouble() ?: 0.0
        val service = state.service

        releaseSlotTimerJob?.cancel() // Annulla il timer precedente, se presente

        val (startDate, endDate) = DateTimeUtils.createStartAndEndTimestamps(
            selectedDate,
            selectedTimeSlot.startTime,
            selectedTimeSlot.endTime
        )
        when (val result = createBookingUseCase(
            service = service,
            startDate = startDate,
            endDate = endDate,
            status = BookingStatus.RESERVED,
            price = price
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

                startReservationTimer()

             //   _newBooking.value = result.data
                val paymentItem = PaymentItem.ServiceBookingPayment.from(
                    service = service,
                    bookingId = result.data.id, // Usa l'ID corretto
                    date = selectedDate,
                    startTime = selectedTimeSlot.startTime,
                    endTime = selectedTimeSlot.endTime,
                    price = price
                )


                _uiEvent.send(BookingUiEvent.NavigateToPayment(paymentItem))
            }

            is Result.Error -> {
                _uiState.update { it.copy(isSlotReserved = false, reservationTimerActive = false) }
                _uiEvent.send(BookingUiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private fun startReservationTimer() {
        reservationJob?.cancel() // cancello il job precedente se esiste
        reservationJob = viewModelScope.launch {
            _uiState.update { it.copy(isSlotReserved = true, reservationTimerActive = true) }
            delay(reservationDuration)
            releaseReservedSlot() // rilascio lo slot riservato dopo il ritardo
            _uiEvent.send(BookingUiEvent.ShowError(UiText.DynamicString("Tempo scaduto. Rieffettua la prenotazione")))
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

            is Result.Error -> _uiEvent.send(BookingUiEvent.ShowError(result.error.asUiText()))
        }
    }

    private suspend fun loadAvailableTimeSlots(date: LocalDate) {
        _uiState.update { it.copy(isLoading = true) }

        when (val result = getAvailableTimeSlotsUseCase(
            date = date,
            serviceDurationMinutes = serviceDurationMinutes
        )) {
            is Result.Success -> {
                _uiState.update {
                    Log.d("BookingViewModel", "loadAvailableTimeSlots: Success: ${result.data}")
                    it.copy(
                        isLoading = false, availableTimeSlots = result.data
                    )
                }
            }

            is Result.Error -> {
                Log.e("BookingViewModel", "loadAvailableTimeSlots: Error: ${result.error}")
                _uiState.update {
                    it.copy(
                        isLoading = false, availableTimeSlots = emptyList()
                    )
                }
                _uiEvent.send(BookingUiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    data class BookingUiState(
        val service: Service,
        val currentBookingId: String? = null,
        val availableTimeSlots: List<TimeSlot> = emptyList(),
        val selectedDate: LocalDate? = null,
        val selectedTimeSlot: TimeSlot? = null,
        val isBookingConfirmed: Boolean = false,
        val isSlotReserved: Boolean = false,
        val reservationTimerActive: Boolean = false,
        val isClosedDay: Boolean = false,
        val isLoading: Boolean = false,
    )

    sealed interface BookingUiEvent {
        data class ShowUserMassage(val message: UiText) : BookingUiEvent
        data class ShowError(val message: UiText) : BookingUiEvent
        data class NavigateToPayment(val paymentItem: PaymentItem.ServiceBookingPayment) :
            BookingUiEvent
    }
}