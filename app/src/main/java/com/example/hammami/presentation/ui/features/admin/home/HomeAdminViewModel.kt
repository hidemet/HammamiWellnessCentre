package com.example.hammami.presentation.ui.features.admin.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.usecase.booking.GetTodayBookingsUseCase
import com.example.hammami.core.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeAdminViewModel @Inject constructor(
    private val getTodayBookingsUseCase: GetTodayBookingsUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Usa stateIn per convertire il Flow in StateFlow
    val state: StateFlow<HomeUiState> = getTodayBookingsUseCase()
        .map { result ->  // Mappa il Result<List<Booking>> in HomeUiState
            when (result) {
                is Result.Success -> HomeUiState(bookings = result.data, isLoading = false)
                is Result.Error -> {
                    // Invia un evento per mostrare lo snackbar
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.error.asUiText()))
                    HomeUiState(isLoading = false) // Mantieni i dati precedenti/default in caso di errore
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Timeout di 5 secondi (opzionale)
            initialValue = HomeUiState(isLoading = true) // Stato iniziale durante il caricamento
        )


    data class HomeUiState(
        val bookings: List<Booking> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class UiEvent {
        data class ShowSnackbar(val message: UiText) : UiEvent()
    }
}