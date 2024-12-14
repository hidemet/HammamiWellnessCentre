package com.example.hammami.presentation.ui.features.payment

import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.domain.model.payment.PaymentSystem

data class PaymentUiState(
    val paymentItem: PaymentItem,
    val selectedMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val appliedVoucher: Voucher? = null,
    val creditCard: CreditCard? = null,
    val cardValidationErrors: CardValidationErrors? = null,
    val finalAmount: Double = 0.0,
    val earnedPoints: Int = 0,
    val totalPoints: Int = 0,
    val discountCode: String = "",
    val discountError: UiText? = null,
    val isPaymentEnabled: Boolean = false,
    val isLoading: Boolean = false,
) {

    data class CardValidationErrors(
        val numberError: UiText? = null,
        val expiryError: UiText? = null,
        val cvvError: UiText? = null
    ) {
        fun isCardValid() = numberError == null && expiryError == null && cvvError == null
    }
}
