package com.example.hammami.domain.model.payment

interface PaymentSystem

data class CreditCardPayment(
    val creditCard: CreditCard
) : PaymentSystem

data class GooglePayPayment(
    val token: String
) : PaymentSystem

data class PayPalPayment(
    val token: String
) : PaymentSystem

enum class PaymentMethod {
    CREDIT_CARD,
    PAYPAL,
    GOOGLE_PAY
}