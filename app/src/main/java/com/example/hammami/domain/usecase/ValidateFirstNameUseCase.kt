package com.example.hammami.domain.usecase

class ValidateFirstNameUseCase {
    operator fun invoke(firstName: String): Result<Unit, FirstNameError> {
        return when {
            firstName.isBlank() -> Result.Error(FirstNameError.EMPTY)
            firstName.length < 2 -> Result.Error(FirstNameError.TOO_SHORT)
            else -> Result.Success(Unit)
        }
    }

    enum class FirstNameError : Error {
        EMPTY,
        TOO_SHORT
    }
}