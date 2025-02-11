package com.example.hammami.domain.usecase.validation.creditCard



import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError.Card.NumberError
import javax.inject.Inject

class ValidateCardNumberUseCase @Inject constructor() {
    operator fun invoke(cardNumber: String): Result<Unit, NumberError> {

        return when {
            cardNumber.isBlank() -> Result.Error(NumberError.EMPTY)
            cardNumber.length != 16 -> Result.Error(NumberError.INVALID_LENGTH)
            else -> Result.Success(Unit)
        }
    }
}
