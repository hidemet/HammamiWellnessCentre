package com.example.hammami.domain.usecase.validation.user

import com.example.hammami.domain.error.ValidationError.User.PhoneNumberError
import com.example.hammami.core.result.Result

class ValidatePhoneNumberUseCase {
    private val phoneRegex = "^\\d{10}$".toRegex()

    operator fun invoke(phoneNumber: String): Result<Unit, PhoneNumberError> {
        return when {
            phoneNumber.isEmpty() -> Result.Error(PhoneNumberError.EMPTY)
            !phoneRegex.matches(phoneNumber) -> Result.Error(PhoneNumberError.INVALID_FORMAT)
            else -> Result.Success(Unit)
        }
    }
}