package com.example.hammami.domain.usecase.validation.user

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError.User.FirstNameError

class ValidateFirstNameUseCase {
    operator fun invoke(firstName: String): Result<Unit, FirstNameError> {
        return when {
            firstName.isBlank() -> Result.Error(FirstNameError.EMPTY)
            firstName.length < 2 -> Result.Error(FirstNameError.TOO_SHORT)
            else -> Result.Success(Unit)
        }
    }

}