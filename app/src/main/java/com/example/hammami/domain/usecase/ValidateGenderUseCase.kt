package com.example.hammami.domain.usecase

class ValidateGenderUseCase {
    operator fun invoke(gender: String): Result<Unit, GenderError> {
        return when {
            gender.isBlank() -> Result.Error(GenderError.EMPTY)
            else -> Result.Success(Unit)
        }
    }

    enum class GenderError : Error {
        EMPTY
    }
}