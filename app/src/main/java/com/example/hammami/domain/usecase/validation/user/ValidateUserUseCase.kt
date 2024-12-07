package com.example.hammami.domain.usecase.validation.user

import com.example.hammami.domain.error.ValidationError.User.*
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.validation.account.ValidateEmailUseCase
import javax.inject.Inject

class ValidateUserUseCase @Inject constructor(
    private val validateBirthDate: ValidateBirthDateUseCase,
    private val validateFirstName: ValidateFirstNameUseCase,
    private val validateLastNameUseCase: ValidateLastNameUseCase,
    private val validatePhoneNumber: ValidatePhoneNumberUseCase,
    private val validateEmail: ValidateEmailUseCase
) {
    operator fun invoke(user: User): ValidationResult {
        val birthDateResult = validateBirthDate(user.birthDate)
        val firstNameResult = validateFirstName(user.firstName)
        val lastNameResult = validateLastNameUseCase(user.lastName)
        val phoneNumberResult = validatePhoneNumber(user.phoneNumber)
        val emailResult = validateEmail(user.email)
        return ValidationResult(
            birthDateError = if (birthDateResult is Result.Success) null else (birthDateResult as? Result.Error)?.error,
            firstNameError = if (firstNameResult is Result.Success) null else (firstNameResult as? Result.Error)?.error,
            lastNameError = if (lastNameResult is Result.Success) null else (lastNameResult as? Result.Error)?.error,
            phoneNumberError = if (phoneNumberResult is Result.Success) null else (phoneNumberResult as? Result.Error)?.error,
            emailError = if (emailResult is Result.Success) null else (emailResult as? Result.Error)?.error
        )
    }

    data class ValidationResult(
        val birthDateError: BirthDateError?,
        val firstNameError: FirstNameError?,
        val lastNameError: LastNameError?,
        val phoneNumberError: PhoneNumberError?,
        val emailError: EmailError?
    ) {
        fun hasErrors() = listOfNotNull(
            birthDateError, firstNameError, lastNameError, phoneNumberError, emailError
        ).isNotEmpty()
    }
}