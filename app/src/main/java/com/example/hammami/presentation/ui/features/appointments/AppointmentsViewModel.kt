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
    private val getUserBookingsSeparatedUseCase: GetUserBookingSeparatedUseCase
): ViewModel() {

    private val _newAppointments = MutableStateFlow<List<Booking>>(emptyList())
    val newAppointments = _newAppointments.asStateFlow()

    private val _pastAppointments = MutableStateFlow<List<Booking>>(emptyList())
    val pastAppointments = _pastAppointments.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadUserData()
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

    fun loadUserBookingsSeparated(userId: String) {
        viewModelScope.launch {
            when (val result = getUserBookingsSeparatedUseCase(userId)) {
                is Result.Success -> {
                    val (pastBookings, futureBookings) = result.data
                    _pastAppointments.update { pastBookings }
                    _newAppointments.update { futureBookings }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun loadUserData(){
        viewModelScope.launch {
            when (val result = getCurrentUserDataUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(user = result.data) }
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }

                else -> {}
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

    data class UiState(
        val user: User? = null
    )

    fun returnUserEmail(): String {
        return FirebaseAuth.getInstance().currentUser?.email.toString()
    }

    fun requireCurrentUser(): User =
        uiState.value.user ?: throw IllegalStateException("User not found")

}