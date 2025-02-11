package com.example.hammami.domain.usecase.validation.review

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError
import javax.inject.Inject

class ValidateReviewTextUseCase @Inject constructor() {
    operator fun invoke(text: String): Result<Unit, ValidationError.Review.TextError> {
        return if (text.isBlank()) {
            Result.Error(ValidationError.Review.TextError.EMPTY)
        } else {
            Result.Success(Unit)
        }
    }
}