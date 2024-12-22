package com.example.hammami.presentation.ui.features.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.model.Service
import com.example.hammami.core.result.Result
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.usecase.booking.CreateBookingUseCase
import com.example.hammami.domain.usecase.booking.GetAvailableSlotsUseCase
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class BookingSlot(
    val startTime: LocalTime,
    val endTime: LocalTime
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
    private val createBookingUseCase: CreateBookingUseCase
   // private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

//    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
//    val selectedDate = _selectedDate.asStateFlow()
//
//    private val _availableSlots = MutableStateFlow<List<BookingSlot>>(emptyList())
//    val availableSlots = _availableSlots.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var serviceId: String = ""
    private var serviceDuration: Int = 0
    private var serviceName: String = ""
    private var servicePrice: Float = 0f

    fun initializeBooking(
        serviceId: String,
        serviceDuration: Int,
        serviceName: String,
        servicePrice: Float
    ) {
        this.serviceId = serviceId
        this.serviceDuration = serviceDuration
        this.serviceName = serviceName
        this.servicePrice = servicePrice
    }


//    private val _selectedSlot = MutableStateFlow<BookingSlot?>(null)
//    val selectedSlot = _selectedSlot.asStateFlow()


    fun onDateSelected(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date)
        getAvailableSlots(date)
    }



    private fun getAvailableSlots(date: LocalDate) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = getAvailableSlotsUseCase(
                date = date.toString(),
                serviceDuration = serviceDuration
            )) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        availableSlots = result.data
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                    )
                    _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }


    fun onSlotSelected(slot: BookingSlot) {
        viewModelScope.launch {
            val paymentItem = PaymentItem.ServiceBookingPayment(
                serviceId = serviceId,
                serviceName = serviceName,
                date = state.value.selectedDate.toString(),
                startTime = slot.startTime.toString(),
                endTime = slot.endTime.toString(),
                price = servicePrice.toDouble()
            )
            _uiEvent.emit(UiEvent.NavigateToPayment(paymentItem))
        }
    }

//    fun generateAvailableSlots(date: LocalDate, serviceDuration: Int) {
//        viewModelScope.launch {
//            val slots = mutableListOf<BookingSlot>()
//
//            // Inizia dalle 10:00
//            var currentTime = LocalTime.of(10, 0)
//
//            // Se è oggi, inizia dall'ora successiva "tonda"
//            if (date == LocalDate.now()) {
//                val now = LocalTime.now()
//                currentTime = now.withMinute(0).plusHours(1)
//                if (currentTime.isBefore(LocalTime.of(10, 0))) {
//                    currentTime = LocalTime.of(10, 0)
//                }
//            }
//
//            // L'ultimo slot deve finire entro le 19:00
//            val lastPossibleStart = LocalTime.of(19, 0).minusMinutes(serviceDuration.toLong())
//
//            while (!currentTime.isAfter(lastPossibleStart)) {
//                val slotEndTime = currentTime.plusMinutes(serviceDuration.toLong())
//
//                // Verifica disponibilità
//                val isAvailable = checkSlotAvailability(
//                    date,
//                    currentTime,
//                    slotEndTime
//                )
//
//                // Aggiungi solo gli slot disponibili
//                if (isAvailable) {
//                    slots.add(BookingSlot(
//                        startTime = currentTime,
//                        endTime = slotEndTime
//                    ))
//                }
//
//                // Passa al prossimo slot "tondo"
//                currentTime = slotEndTime.withMinute(0)
//                if (slotEndTime.minute > 0) {
//                    currentTime = currentTime.plusHours(1)
//                }
//            }
//
//            _availableSlots.emit(slots)
//        }
//    }

//    private suspend fun checkSlotAvailability(
//        date: LocalDate,
//        startTime: LocalTime,
//        endTime: LocalTime
//    ): Boolean {
//        return when (val result = bookingRepository.getAvailableOperators(
//            date.toString(),
//            startTime.toString(),
//            endTime.toString()
//        )) {
//            is Result.Success -> result.data.isNotEmpty()
//            is Result.Error -> false
//        }
//    }
//
//    fun selectSlot(slot: BookingSlot) {
//        _selectedSlot.value = slot
//    }

    data class UiState(
        val isLoading: Boolean = false,
        val selectedDate: LocalDate? = null,
        val availableSlots: List<BookingSlot> = emptyList()
    )

    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class NavigateToPayment(val paymentItem: PaymentItem) : UiEvent()
    }
}