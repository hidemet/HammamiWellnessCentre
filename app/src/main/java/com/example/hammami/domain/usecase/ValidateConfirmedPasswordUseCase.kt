package com.example.hammami.domain.usecase


class ValidateConfirmedPasswordUseCase() {
    operator fun invoke(password: String, confirmPassword: String): Result<Unit, ConfirmedPasswordError> {
        return when {
            password != confirmPassword -> Result.Error(ConfirmedPasswordError.PASSWORDS_DO_NOT_MATCH)
            else -> Result.Success(Unit)
        }
    }

    enum class ConfirmedPasswordError : Error {
        PASSWORDS_DO_NOT_MATCH
    }
}