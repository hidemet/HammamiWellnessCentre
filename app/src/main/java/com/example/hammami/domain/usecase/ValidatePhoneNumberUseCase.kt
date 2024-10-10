package com.example.hammami.domain.usecase

class ValidatePhoneNumberUseCase {
    private val phoneRegex = "^\\d{10}$".toRegex()

    operator fun invoke(phoneNumber: String): Result<Unit, PhoneNumberError> {
        return when {
            phoneNumber.isEmpty() -> Result.Error(PhoneNumberError.EMPTY)
            !phoneRegex.matches(phoneNumber) -> Result.Error(PhoneNumberError.INVALID_FORMAT)
            else -> Result.Success(Unit)
        }
    }

    enum class PhoneNumberError : Error {
        EMPTY,
        INVALID_FORMAT
    }
}