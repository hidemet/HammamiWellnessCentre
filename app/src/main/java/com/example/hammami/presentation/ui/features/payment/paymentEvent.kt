package com.example.hammami.presentation.ui.features.payment

import com.example.hammami.core.ui.UiText

sealed class PaymentEvent {
    data class ShowError(val message: UiText) : PaymentEvent()
    data class NavigateToGiftCardGenerated(val transactionId: String) : PaymentEvent()
    data class NavigateToBookingSummary(val transactionId: String) : PaymentEvent()
    object NavigateBack : PaymentEvent()
}