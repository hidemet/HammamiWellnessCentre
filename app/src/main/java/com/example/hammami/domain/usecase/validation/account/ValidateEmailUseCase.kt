package com.example.hammami.domain.usecase.validation.account

import com.example.hammami.domain.error.ValidationError.User.EmailError
import com.example.hammami.core.validator.AndroidEmailPatternValidator
import com.example.hammami.core.result.Result
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor(
    private val validator: AndroidEmailPatternValidator
) {
    operator fun invoke(email: String): Result<Unit, EmailError> {
        return validator.isValidEmail(email)
    }
}