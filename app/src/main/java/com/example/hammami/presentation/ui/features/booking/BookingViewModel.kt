package com.example.hammami.presentation.ui.features.booking


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.booking.CancelBookingUseCase
import com.example.hammami.domain.usecase.booking.CreateBookingUseCase
import com.example.hammami.domain.usecase.booking.GetAvailableTimeSlotsUseCase
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
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getAvailableTimeSlotsUseCase: GetAvailableTimeSlotsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val createBookingUseCase: CreateBookingUseCase
    //private val scheduleServiceUseCase: ScheduleServiceUseCase,
    // private val reserveTimeSlotUseCase: ReserveTimeSlotUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BookingUiEvent>()
    val uiEvent: SharedFlow<BookingUiEvent> = _uiEvent.asSharedFlow()

    private var reservationJob: Job? = null
    private val reservationDuration = 15.minutes

//    private val _availableTimeSlots = MutableStateFlow<Result<List<String>, DataError>>(Result.Success(emptyList()))
//    val availableTimeSlots: StateFlow<Result<List<String>, DataError>> = _availableTimeSlots
//
//    private val _bookingResult = MutableStateFlow<Result<Unit, DataError>?>(null)
//    val bookingResult: StateFlow<Result<Unit, DataError>?> = _bookingResult

    fun onServiceChanged(service: Service) {
        _uiState.update { it.copy(service = service) }
    }

     fun onDateSelected(date: Date) {
        _uiState.update { it.copy(selectedDate = date, availableTimeSlots = emptyList()) }
        viewModelScope.launch {
            loadAvailableTimeSlots(date)
        }
    }

    fun resetBookingConfirmation() {
        _uiState.update { it.copy(isBookingConfirmed = false) }
    }

    fun onTimeSlotSelected(timeSlot: String) = viewModelScope.launch {
        _uiState.update { it.copy(selectedTimeSlot = timeSlot) }
        reserveSlot()
    }


    private suspend fun reserveSlot() {
        val service = uiState.value.service ?: return
        val selectedDate = uiState.value.selectedDate ?: return
        val selectedTimeSlot = uiState.value.selectedTimeSlot ?: return

        when (val result = createBookingUseCase(
            service = service,
            selectedDate = selectedDate,
            selectedTimeSlot = selectedTimeSlot,
            status = BookingStatus.RESERVED
        )) {
            is Result.Success -> {
                _uiState.update { it.copy(currentBookingId = result.data.id) }
                startReservationTimer()
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
            delay(reservationDuration)
            releaseTimeSlot() // rilascio lo slot riservato dopo il ritardo
            _uiEvent.emit(BookingUiEvent.ShowError(UiText.DynamicString("Tempo scaduto. Rieffettua la prenotazione")))
        }
    }

    private suspend fun releaseTimeSlot() {
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

    fun scheduleBooking() = viewModelScope.launch {
        val state = uiState.value
        if (state.service == null || state.selectedDate == null || state.selectedTimeSlot == null || !uiState.value.isSlotReserved) {
            _uiEvent.emit(BookingUiEvent.ShowError(UiText.DynamicString("Please reserve a time slot before booking.")))
            return@launch
        }

        if (!state.isSlotReserved) {
            _uiEvent.emit(BookingUiEvent.ShowError(UiText.DynamicString("Si prega di riservare uno slot orario prima di prenotare.")))
            return@launch
        }

        _uiState.update { it.copy(isLoading = true) }
        reservationJob?.cancel() // Cancel the reservation timer

        when (val result = createBookingUseCase(
            state.service,
            state.selectedDate,
            state.selectedTimeSlot,
            BookingStatus.CONFIRMED
        )) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBookingConfirmed = true,
                        isSlotReserved = false,
                        reservationTimerActive = false
                    )
                }
                _uiEvent.emit(BookingUiEvent.BookingSuccess)
            }

            is Result.Error -> {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.emit(BookingUiEvent.ShowError(result.error.asUiText()))
            }
        }
    }


    private suspend fun loadAvailableTimeSlots(date: Date) {
        val service = uiState.value.service ?: return
        _uiState.update { it.copy(isLoading = true) }

        when (val result = getAvailableTimeSlotsUseCase(
            serviceId = service.id,
            date = date,
            serviceDurationMinutes = service.length?.toInt() ?: 60
        )) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableTimeSlots = result.data
                    )
                }
            }

            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableTimeSlots = emptyList()
                    )
                }
                _uiEvent.emit(BookingUiEvent.ShowError(result.error.asUiText()))
            }
        }

    }

//    private suspend fun createBooking(
//        service: Service,
//        selectedDate: Date,
//        selectedTimeSlot: String,
//        status: BookingStatus
//    ): Booking {
//        val calendar = Calendar.getInstance()
//        calendar.time = selectedDate
//
//        val hour = selectedTimeSlot.split(":")[0].toInt()
//        val minute = selectedTimeSlot.split(":")[1].toInt()
//        calendar.set(Calendar.HOUR_OF_DAY, hour)
//        calendar.set(Calendar.MINUTE, minute)
//
//        val endTimeCalendar = calendar.clone() as Calendar
//        endTimeCalendar.add(Calendar.MINUTE, service.length?.toInt() ?: 60)
//
//        val bookingEndTime = String.format(
//            "%02d:%02d",
//            endTimeCalendar.get(Calendar.HOUR_OF_DAY),
//            endTimeCalendar.get(Calendar.MINUTE)
//        )
//
//        when (val resultBooking = createBookingUseCase(
//            serviceId = service.id,
//            serviceName = service.name,
//            date = calendar.time,
//            startTime = selectedTimeSlot,
//            endTime = bookingEndTime,
//            status = BookingStatus.CONFIRMED
//        )) {
//            is Result.Success -> resultBooking.data
//            is Result.Error -> _uiEvent.emit(BookingUiEvent.ShowError(resultBooking.error.asUiText()))
//        }
////    }


//    fun onConfirmDateTime(service: Service, selectedDate: Date, selectedTimeSlot: String) {
//        _uiState.update { it.copy(isLoading = true) } // Mostra il caricamento mentre si naviga
//
//        // Emetti un evento per navigare verso il pagamento
//        viewModelScope.launch {
//            _uiEvent.emit(
//                BookingUiEvent.NavigateToPayment(
//                    service = service,
//                    selectedDate = selectedDate,
//                    selectedTimeSlot = selectedTimeSlot
//                )
//            )
//            _uiState.update { it.copy(isLoading = false) }
//        }
//
//    }


    data class BookingUiState(
        val service: Service? = null,
        val currentBookingId: String? = null,
        val availableTimeSlots: List<String> = emptyList(),
        val selectedDate: Date? = null,
        val selectedTimeSlot: String? = null,
        val isBookingConfirmed: Boolean = false,
        val isSlotReserved: Boolean = false,
        val reservationTimerActive: Boolean = false,
        val isLoading: Boolean = false,
    )

    sealed interface BookingUiEvent {
        data class ShowUserMassage(val message: UiText) : BookingUiEvent
        data class ShowError(val message: UiText) : BookingUiEvent
        object BookingSuccess : BookingUiEvent
        data class NavigateToPayment(
            val service: Service,
            val selectedDate: Date,
            val selectedTimeSlot: String
        ) : BookingUiEvent
    }
}