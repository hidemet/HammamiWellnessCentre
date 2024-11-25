package com.example.hammami.domain.usecase.validation.user

import com.example.hammami.domain.error.ValidationError.User.LastNameError
import com.example.hammami.core.result.Result

class ValidateLastNameUseCase {
    operator fun invoke(lastName: String): Result<Unit, LastNameError> {
        return when {
            lastName.isBlank() -> Result.Error(LastNameError.EMPTY)
            else -> Result.Success(Unit)
        }
    }
}