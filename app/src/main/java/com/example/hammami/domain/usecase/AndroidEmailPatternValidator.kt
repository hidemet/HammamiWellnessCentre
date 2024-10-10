package com.example.hammami.domain.usecase

import javax.inject.Inject

class AndroidEmailPatternValidator @Inject constructor() : EmailPatternValidator {
    override fun isValidEmail(email: String): Result<Unit, ValidateEmailUseCase.EmailError> {
        return when {
            email.isBlank() -> Result.Error(ValidateEmailUseCase.EmailError.EMPTY)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Result.Error(ValidateEmailUseCase.EmailError.INVALID_FORMAT)
            else -> Result.Success(Unit)
        }
    }
}