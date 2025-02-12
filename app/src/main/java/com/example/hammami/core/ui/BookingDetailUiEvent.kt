package com.example.hammami.core.ui

sealed class BookingDetailUiEvent {
    data class ShowError(val message: UiText) : BookingDetailUiEvent()
    object BookingCancelled : BookingDetailUiEvent()
}