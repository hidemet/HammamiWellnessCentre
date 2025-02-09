package com.example.hammami.core.ui

import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.ItemProfileOption
import com.example.hammami.domain.model.User

data class BookingDetailUiState(
    val booking: Booking? = null,
    val user: User? = null,
    val isLoading: Boolean = false,
    val availableOptions: List<ItemProfileOption> = emptyList(), // Opzioni disponibili
    val isNavigationOptionVisible: Boolean = false // Flag per la visibilità dell'opzione di navigazione
)