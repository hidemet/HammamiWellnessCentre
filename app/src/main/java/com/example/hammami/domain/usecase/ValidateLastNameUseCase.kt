package com.example.hammami.domain.usecase

class ValidateLastNameUseCase {
    operator fun invoke(lastName: String): Result<Unit, LastNameError> {
        return when {
            lastName.isBlank() -> Result.Error(LastNameError.EMPTY)
            else -> Result.Success(Unit)
        }
    }

    enum class LastNameError : Error {
        EMPTY
    }
}