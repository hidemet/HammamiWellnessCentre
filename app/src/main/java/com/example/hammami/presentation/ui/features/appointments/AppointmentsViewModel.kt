package com.example.hammami.presentation.ui.features.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.GetCurrentUserDataUseCase
import com.example.hammami.domain.usecase.appointment.GetNewAppointmentUseCase
import com.example.hammami.domain.usecase.appointment.GetPastAppointmentUseCase
import com.example.hammami.domain.usecase.booking.GetUserBookingsUseCase
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.domain.usecase.user.GetUserBookingSeparatedUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getCurrentUserDataUseCase: GetCurrentUserDataUseCase,
    private val getUserBookingsSeparatedUseCase: GetUserBookingSeparatedUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
): ViewModel() {

    private val _newAppointments = MutableStateFlow<List<Booking>>(emptyList())
    val newAppointments = _newAppointments.asStateFlow()

    private val _pastAppointments = MutableStateFlow<List<Booking>>(emptyList())
    val pastAppointments = _pastAppointments.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userIdResult = getCurrentUserIdUseCase()
            if (userIdResult is Result.Success) {
                val userId = userIdResult.data
                loadUserData(userId)
                loadUserBookings(userId)
            } else if (userIdResult is Result.Error) {
                emitUiEvent(UiEvent.ShowError(userIdResult.error.asUiText()))
                _uiState.update { it.copy(isLoading = false) } // Fine caricamento
            }
        }
    }

    private fun loadUserData(userId: String){
        viewModelScope.launch {
            when (val result = getCurrentUserDataUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(user = result.data, isLoading = false) }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }


    fun refreshData() {
        viewModelScope.launch {
            val userIdResult = getCurrentUserIdUseCase()
            if (userIdResult is Result.Success) {
                loadUserBookings(userIdResult.data)

            } else if (userIdResult is Result.Error) {
                _uiEvent.emit( UiEvent.ShowError(userIdResult.error.asUiText()))
            }
        }
    }

    /*
    fun loadData() {
        viewModelScope.launch {
            loadBenessereData()
        }
    }
     */

    /*
    fun loadNewAppointmentsData(clientEmail: String){
        viewModelScope.launch {
            when (val result = getNewAppointmentUseCase(clientEmail)) {
                is Result.Success -> {
                    _newAppointments.update { result.data }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }

                else -> {}
            }
        }
    }
     */

    /*
    fun loadPastAppointmentsData(clientEmail: String){
        viewModelScope.launch {
            when (val result = getPastAppointmentUseCase(clientEmail)) {
                is Result.Success -> {
                    _pastAppointments.update { result.data }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }

                else -> {}
            }
        }
    }
     */

    /*
    fun loadPastAppointmentsData(clientId: String){
        viewModelScope.launch {
            when (val result = getPastAppointmentUseCase(clientId)) {
                is Result.Success -> {
                    _pastAppointments.update { result.data }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }

                else -> {}
            }
        }
    }

    fun loadNewAppointmentsData(clientId: String){
        viewModelScope.launch {
            when (val result = getNewAppointmentUseCase(clientId)) {
                is Result.Success -> {
                    _newAppointments.update { result.data }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }

                else -> {}
            }
        }
    }

     */

    fun loadUserBookings(userId: String) {
        viewModelScope.launch {
            when (val result = getUserBookingsSeparatedUseCase(userId)) {
                is Result.Success -> {
                    val (pastBookings, futureBookings) = result.data
                    _uiState.update {
                        it.copy(
                            pastAppointments = pastBookings,
                            newAppointments = futureBookings,
                            isLoading = false // Fine caricamento
                        )
                    }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                    _uiState.update { it.copy(isLoading = false) }  // Fine caricamento in caso di errore

                }
            }
        }
    }

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }


    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }

    data class AppointmentsUiState(
        val user: User? = null,
        val newAppointments: List<Booking> = emptyList(),
        val pastAppointments: List<Booking> = emptyList(),
        val isLoading: Boolean = false
    )
}