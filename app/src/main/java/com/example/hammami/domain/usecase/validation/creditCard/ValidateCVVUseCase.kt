package com.example.hammami.domain.usecase.validation.creditCard

import com.example.hammami.domain.error.ValidationError.Card.CvvError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class ValidateCVVUseCase @Inject constructor() {
    operator fun invoke(cvv: String): Result<Unit, CvvError> {
        return when {
            cvv.isBlank() -> Result.Error(CvvError.EMPTY)
            cvv.length != 3 -> Result.Error(CvvError.INVALID_LENGTH)
            !cvv.all { it.isDigit() } -> Result.Error(CvvError.INVALID_FORMAT)
            else -> Result.Success(Unit)
        }
    }
}