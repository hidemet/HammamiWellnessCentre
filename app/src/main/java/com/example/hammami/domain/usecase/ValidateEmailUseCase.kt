package com.example.hammami.domain.usecase


import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor(
    private val validator: AndroidEmailPatternValidator
) {
    operator fun invoke(email: String): Result<Unit, EmailError> {
        return validator.isValidEmail(email)
    }

    enum class EmailError : Error {
        EMPTY,
        INVALID_FORMAT
    }
}