package com.example.hammami.domain.usecase.validation.creditCard



import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError
import com.example.hammami.domain.error.ValidationError.Card.NumberError
import javax.inject.Inject

class ValidateCardNumberUseCase @Inject constructor() {
    operator fun invoke(cardNumber: String): Result<Unit, ValidationError.Card.NumberError> {

        return when {
            cardNumber.isBlank() -> Result.Error(NumberError.EMPTY)
            cardNumber.length != 16 -> Result.Error(NumberError.INVALID_LENGTH)
            !isLuhnValid(cardNumber) -> Result.Error(NumberError.INVALID_CARD)
            else -> Result.Success(Unit)
        }
    }

    private fun isLuhnValid(number: String): Boolean {
        var sum = 0
        var alternate = false

        for (i in number.length - 1 downTo 0) {
            var n = number[i].toString().toInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }

        return (sum % 10 == 0)
    }
}
