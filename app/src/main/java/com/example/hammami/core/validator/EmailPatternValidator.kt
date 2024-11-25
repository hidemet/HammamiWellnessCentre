package com.example.hammami.core.validator

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError.User.EmailError


interface EmailPatternValidator {
    fun isValidEmail(email: String): Result<Unit, EmailError>
}