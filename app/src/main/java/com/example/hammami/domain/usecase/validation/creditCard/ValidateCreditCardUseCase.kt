package com.example.hammami.domain.usecase.validation.creditCard

import com.example.hammami.domain.error.ValidationError.*
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.core.result.Result
import javax.inject.Inject

class ValidateCreditCardUseCase @Inject constructor(
    private val validateNumber: ValidateCardNumberUseCase,
    private val validateExpiry: ValidateExpiryDateUseCase,
    private val validateCVV: ValidateCVVUseCase
) {
    operator fun invoke(creditCard: CreditCard): ValidationResult {
        val numberResult = validateNumber(creditCard.number)
        val expiryResult = validateExpiry(creditCard.expiryDate)
        val cvvResult = validateCVV(creditCard.cvv)

        return ValidationResult(
            numberError = if (numberResult is Result.Success) null else (numberResult as? Result.Error)?.error,
            expiryError = if (expiryResult is Result.Success) null else (expiryResult as? Result.Error)?.error,
            cvvError = if (cvvResult is Result.Success) null else (cvvResult as? Result.Error)?.error
        )
    }

    data class ValidationResult(
        val numberError: Card.NumberError?,
        val expiryError: Card.ExpiryDateError?,
        val cvvError: Card.CvvError?
    ) {
        fun isCardValid() = numberError == null && expiryError == null && cvvError == null
    }
}