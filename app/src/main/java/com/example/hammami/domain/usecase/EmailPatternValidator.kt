package com.example.hammami.domain.usecase
import com.example.hammami.domain.usecase.Result



interface EmailPatternValidator {
    fun isValidEmail(email: String): Result<Unit, ValidateEmailUseCase.EmailError>
}