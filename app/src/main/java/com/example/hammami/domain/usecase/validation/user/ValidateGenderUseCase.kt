package com.example.hammami.domain.usecase.validation.user

import com.example.hammami.domain.error.ValidationError.User.GenderError
import com.example.hammami.core.result.Result

class ValidateGenderUseCase {
    operator fun invoke(gender: String): Result<Unit, GenderError> {
        return when {
            gender.isBlank() -> Result.Error(GenderError.EMPTY)
            else -> Result.Success(Unit)
        }
    }

}