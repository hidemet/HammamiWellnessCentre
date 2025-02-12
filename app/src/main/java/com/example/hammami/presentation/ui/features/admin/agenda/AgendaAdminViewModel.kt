package com.example.hammami.presentation.ui.features.admin.agenda

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.time.DateTimeUtils.toEndOfDayTimestamp
import com.example.hammami.core.time.DateTimeUtils.toStartOfDayTimestamp
import com.example.hammami.core.time.DateTimeUtils.toTimestamp
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.usecase.booking.GetBookingsForDateRangeUseCase
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.hammami.core.result.Result
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AgendaAdminViewModel @Inject constructor(
    private val getBookingsForDateRangeUseCase: GetBookingsForDateRangeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaUiState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _selectedDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val selectedDateRange: StateFlow<Pair<Long, Long>?> = _selectedDateRange.asStateFlow()


    init {
        loadBookingsForCurrentWeek()  //Carica le prenotazioni della settimana corrente.
    }

    fun onDateRangeSelected(startDate: Long, endDate: Long) {
        _selectedDateRange.value = Pair(startDate, endDate) // Salva le date in millisecondi
        Log.d("AgendaAdminViewModel", "Date range selected: $startDate - $endDate")
        loadBookings() //Ricarico quando cambia la selezione
    }



    private fun loadBookings() {
        val selectedRange = _selectedDateRange.value
        if (selectedRange != null) {
            val startDate = Timestamp(Date(selectedRange.first))
            val endDate = Timestamp(Date(selectedRange.second))

            // Usiamo il Flow direttamente.
            getBookingsForDateRangeUseCase(startDate, endDate)
                .onEach { _state.update { it.copy(isLoading = true) } } //Mostro il caricamento *prima*
                .map { result ->
                    when (result) {
                        is Result.Success -> AgendaUiState(
                            bookings = result.data,
                            isLoading = false
                        )

                        is Result.Error -> {
                            emitUiEvent(UiEvent.ShowSnackbar(result.error.asUiText()))
                            AgendaUiState(isLoading = false) // Mantieni i dati precedenti in caso di errore
                        }
                    }
                }
                .onEach { newState -> _state.value = newState } //Aggiorno lo stato.
                .launchIn(viewModelScope)  //Avvio la raccolta nel viewModelScope.

        } else { // Nessun range selezionato.
            loadBookingsForCurrentWeek()  // mostro settimana corrente
        }
    }


    //Funzione per caricare la settimana corrente
    fun loadBookingsForCurrentWeek() {
        val today = LocalDate.now()
        val startOfDay = today.with(DayOfWeek.MONDAY).toStartOfDayTimestamp() // Lunedì
        val endOfDay = today.with(DayOfWeek.SUNDAY).atTime(23,59,59).toTimestamp() // Domenica sera

        getBookingsForDateRangeUseCase(startOfDay, endOfDay)
            .onEach { _state.update { it.copy(isLoading = true) } }
            .map { result ->
                when(result) {
                    is Result.Success -> AgendaUiState(
                        bookings = result.data,
                        isLoading = false,
                        selectedDateRange = Pair(startOfDay.seconds * 1000, endOfDay.seconds * 1000) //millisecondi
                    )
                    is Result.Error -> {
                        emitUiEvent(UiEvent.ShowSnackbar(result.error.asUiText()))
                        AgendaUiState(isLoading = false) // Mantieni i dati precedenti
                    }
                }
            }
            .onEach{ newState -> _state.value = newState } //Aggiorno lo stato.
            .launchIn(viewModelScope)  //Avvio la raccolta nel viewModelScope.
    }


    private suspend fun emitUiEvent(event: UiEvent) {
        _state.update { it.copy(isLoading = false) }
        _uiEvent.emit(event)
    }


    data class AgendaUiState(
        val bookings: List<Booking> = emptyList(),
        val isLoading: Boolean = false,
        val selectedDateRange: Pair<Long, Long>? = null // Coppia di Long (millisecondi)
    )

    sealed class UiEvent {
        data class ShowSnackbar(val message: UiText) : UiEvent()
    }
}