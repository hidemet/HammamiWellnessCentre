package com.example.hammami.core.validator

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError.User.EmailError

import javax.inject.Inject

class AndroidEmailPatternValidator @Inject constructor() : EmailPatternValidator {
    override fun isValidEmail(email: String): Result<Unit, EmailError> {
        return when {
            email.isBlank() -> Result.Error(EmailError.EMPTY)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Result.Error(
              EmailError.INVALID_FORMAT
            )
            else -> Result.Success(Unit)
        }
    }
}