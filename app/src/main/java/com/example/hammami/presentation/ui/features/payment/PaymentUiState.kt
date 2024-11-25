package com.example.hammami.presentation.ui.features.payment

import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.domain.model.payment.Discount
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.domain.model.payment.PaymentSystem

data class PaymentUiState(
    val paymentItem: PaymentItem,
    val selectedMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val paymentSystem: PaymentSystem? = null,
    val appliedDiscount: Discount? = null,
    val creditCard: CreditCard? = null,
    val cardErrors: CardErrors? = null,
    val earnedPoints: Int = 0,
    val totalPoints: Int = 0,
    val isLoading: Boolean = false,
    val discountCode: String = "",
    val discountError: UiText? = null
) {
    data class CardErrors(
        val numberError: UiText? = null,
        val expiryError: UiText? = null,
        val cvvError: UiText? = null
    )
}
