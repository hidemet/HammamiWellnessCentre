package com.example.hammami.domain.usecase.validation.payment

import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.GooglePayPayment
import com.example.hammami.domain.model.payment.PayPalPayment
import com.example.hammami.domain.model.payment.PaymentSystem
import com.example.hammami.domain.usecase.validation.creditCard.ValidateCreditCardUseCase
import javax.inject.Inject

class PaymentValidationUseCase @Inject constructor(
    private val validateCreditCard: ValidateCreditCardUseCase
) {
    operator fun invoke(paymentSystem: PaymentSystem): PaymentValidationResult {
        return when (paymentSystem) {
            is CreditCardPayment -> {
                val cardValidation = validateCreditCard(paymentSystem.creditCard)
                if (cardValidation.isValid) {
                    PaymentValidationResult.Valid
                } else {
                    PaymentValidationResult.InvalidCreditCard(cardValidation)
                }
            }

            is GooglePayPayment -> {
                if (paymentSystem.token.isBlank()) {
                    PaymentValidationResult.InvalidToken
                } else {
                    PaymentValidationResult.Valid
                }
            }

            is PayPalPayment -> {
                if (paymentSystem.token.isBlank()) {
                    PaymentValidationResult.InvalidToken
                } else {
                    PaymentValidationResult.Valid
                }
            }

            else -> {
                PaymentValidationResult.InvalidToken
            }
        }
    }

    sealed class PaymentValidationResult {
        object Valid : PaymentValidationResult()
        data class InvalidCreditCard(val validation: ValidateCreditCardUseCase.ValidationResult) : PaymentValidationResult()

        object InvalidToken : PaymentValidationResult()
    }
}
