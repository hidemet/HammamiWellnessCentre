package com.example.hammami.presentation.ui.features.payment

import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod

data class PaymentUiState(
    val paymentItem: PaymentItem,
    val selectedMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val appliedVoucher: Voucher? = null,
    val creditCard: CreditCard? = null,
    val cardValidationErrors: CardValidationErrors? = null,

    // Prezzi e sconti
    val itemPrice: Double,
    val discountValue: Double? = null,
    val discountCode: String = "",
    val discountError: UiText? = null,
    val finalAmount: Double,

    // Punti karma
    val currentKarmaPoints: Int,
    val earnedKarmaPoints: Int,

    // State UI
    val isPaymentEnabled: Boolean = false,
    val isLoading: Boolean = false,
) {

    val showPriceBreakdown: Boolean = discountValue != null
    val newKarmaPointsBalance: Int = currentKarmaPoints + earnedKarmaPoints

    data class CardValidationErrors(
        val numberError: UiText? = null,
        val expiryError: UiText? = null,
        val cvvError: UiText? = null
    ) {
        fun isCardValid() = numberError == null && expiryError == null && cvvError == null
    }
}
