package com.example.hammami.domain.usecase

class ValidatePasswordUseCase {
   private val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()

    operator fun invoke(password: String): Result<Unit, PasswordError> {
        return when {
            password.isBlank() -> Result.Error(PasswordError.EMPTY)
            !passwordRegex.matches(password) -> Result.Error(PasswordError.INVALID_FORMAT)
            else -> Result.Success(Unit)
        }
    }

    enum class PasswordError : Error {
        EMPTY,
        INVALID_FORMAT
    }
}