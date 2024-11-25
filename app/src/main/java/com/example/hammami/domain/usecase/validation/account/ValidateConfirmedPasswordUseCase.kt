package com.example.hammami.domain.usecase.validation.account

import com.example.hammami.domain.error.ValidationError.User.ConfirmedPasswordError
import com.example.hammami.core.result.Result


class ValidateConfirmedPasswordUseCase() {
    operator fun invoke(
        password: String,
        confirmPassword: String
    ): Result<Unit, ConfirmedPasswordError> {
        return when {
            password != confirmPassword -> Result.Error(ConfirmedPasswordError.PASSWORDS_DO_NOT_MATCH)
            else -> Result.Success(Unit)
        }
    }

}