package com.example.hammami.domain.usecase.validation.creditCard


import com.example.hammami.domain.error.ValidationError.Card.ExpiryDateError
import com.example.hammami.core.result.Result
import java.time.YearMonth
import javax.inject.Inject


class ValidateExpiryDateUseCase @Inject constructor() {
    operator fun invoke(expiryDate: String): Result<Unit, ExpiryDateError> {
        if (expiryDate.isBlank()) return Result.Error(ExpiryDateError.EMPTY)
        if (expiryDate.length != 5) return Result.Error(ExpiryDateError.INVALID_FORMAT)

        val (month, year) = expiryDate.split("/")
        return try {
            val expiry = YearMonth.of(2000 + year.toInt(), month.toInt())
            when {
                expiry < YearMonth.now() -> Result.Error(ExpiryDateError.EXPIRED)
                else -> Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(ExpiryDateError.INVALID_FORMAT)
        }
    }
}