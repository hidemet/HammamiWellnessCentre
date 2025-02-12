package com.example.hammami.domain.usecase.validation.review

import com.example.hammami.domain.error.ValidationError.*
import com.example.hammami.domain.error.ValidationError.Review.*

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError
import javax.inject.Inject

class ValidateReviewRatingUseCase @Inject constructor(){
    operator fun invoke(rating: Float): Result<Unit, RatingError> {
        return if (rating <= 0f) {
            Result.Error(RatingError.INVALID_RATING)
        } else {
            Result.Success(Unit)
        }
    }
}