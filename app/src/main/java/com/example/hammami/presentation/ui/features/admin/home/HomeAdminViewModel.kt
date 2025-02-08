package com.example.hammami.presentation.ui.features.admin.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.usecase.booking.GetTodayBookingsUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.core.utils.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeAdminViewModel @Inject constructor(
    private val getTodayBookingsUseCase: GetTodayBookingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadBookings()
    }

    private fun loadBookings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getTodayBookingsUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(bookings = result.data, isLoading = false) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.error.asUiText()))
                }
            }
        }
    }


    data class HomeUiState(
        val bookings: List<Booking> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class UiEvent {
        data class ShowSnackbar(val message: UiText) : UiEvent()
    }
}