package com.example.hammami.domain.usecase.validation.payment

import com.example.hammami.domain.usecase.validation.creditCard.ValidateCreditCardUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.GooglePayPayment
import com.example.hammami.domain.model.payment.PayPalPayment
import com.example.hammami.domain.model.payment.PaymentSystem
import javax.inject.Inject

class ValidatePaymentSystemUseCase @Inject constructor(
    private val validateCreditCard: ValidateCreditCardUseCase
) {
    operator fun invoke(paymentSystem: PaymentSystem): Result<Unit, DataError> {
        return when (paymentSystem) {
            is CreditCardPayment -> {
                val validation = validateCreditCard(paymentSystem.creditCard)
                if (validation.isValid) Result.Success(Unit)
                else Result.Error(DataError.Payment.INVALID_PAYMENT_INFO)
            }
            is GooglePayPayment -> {
                if (paymentSystem.token.isNotBlank()) Result.Success(Unit)
                else Result.Error(DataError.Payment.INVALID_PAYMENT_INFO)
            }
            is PayPalPayment -> {
                if (paymentSystem.token.isNotBlank()) Result.Success(Unit)
                else Result.Error(DataError.Payment.INVALID_PAYMENT_INFO)
            }
            else -> {
                Result.Error(DataError.Payment.INVALID_PAYMENT_INFO)
            }
        }
    }
}