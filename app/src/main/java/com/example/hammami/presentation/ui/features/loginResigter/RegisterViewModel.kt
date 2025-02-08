package com.example.hammami.presentation.ui.features.loginResigter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.auth.SignUpUseCase
import com.example.hammami.domain.usecase.validation.account.ValidateConfirmedPasswordUseCase
import com.example.hammami.domain.usecase.validation.account.ValidateEmailUseCase
import com.example.hammami.domain.usecase.validation.account.ValidatePasswordUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateBirthDateUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateFirstNameUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateGenderUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateLastNameUseCase
import com.example.hammami.domain.usecase.validation.user.ValidatePhoneNumberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateFirstNameUseCase: ValidateFirstNameUseCase,
    private val validateLastNameUseCase: ValidateLastNameUseCase,
    private val validateBirthDateUseCase: ValidateBirthDateUseCase,
    private val validateGenderUseCase: ValidateGenderUseCase,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateConfirmedPasswordUseCase: ValidateConfirmedPasswordUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationFormState())
    val state = _state.asStateFlow()

    sealed class ValidationResult {

        object Success : ValidationResult()
        data class Error(val errors: Map<String, UiText>) : ValidationResult()
    }

    fun validateAndUpdateStep(step: RegistrationStep, data: Map<String, String>) = flow {
        val result = when (step) {
            RegistrationStep.PERSONAL_INFO -> validatePersonalInfo(data)
            RegistrationStep.HEALTH_INFO -> validateHealthInfo(data)
            RegistrationStep.CONTACT_INFO -> validateContactInfo(data)
            RegistrationStep.CREDENTIALS -> validateCredentials(data)
            RegistrationStep.FRIEND_CODE -> validateFriendCode(data)
        }
        emit(result)
    }

    private fun validatePersonalInfo(data: Map<String, String>): ValidationResult {
        val firstName = data["firstName"] ?: ""
        val lastName = data["lastName"] ?: ""

        val firstNameResult = validateFirstNameUseCase(firstName)
        val lastNameResult = validateLastNameUseCase(lastName)

        val errors = mutableMapOf<String, UiText>()
        if (firstNameResult is Result.Error) errors["firstName"] = firstNameResult.error.asUiText()
        if (lastNameResult is Result.Error) errors["lastName"] = lastNameResult.error.asUiText()

        _state.value = _state.value.copy(
            firstName = firstName,
            lastName = lastName,
            firstNameError = errors["firstName"],
            lastNameError = errors["lastName"]
        )

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Error(errors)
    }

    private fun validateHealthInfo(data: Map<String, String>): ValidationResult {
        val birthDate = data["birthDate"] ?: ""
        val gender = data["gender"] ?: ""
        val allergies = data["allergies"] ?: ""
        val disabilities = data["disabilities"] ?: ""

        val birthDateResult = validateBirthDateUseCase(birthDate)
        val genderResult = validateGenderUseCase(gender)

        val errors = mutableMapOf<String, UiText>()
        if (birthDateResult is Result.Error) errors["birthDate"] = birthDateResult.error.asUiText()
        if (genderResult is Result.Error) errors["gender"] = genderResult.error.asUiText()

        _state.value = _state.value.copy(
            birthDate = birthDate,
            gender = gender,
            allergies = allergies,
            disabilities = disabilities,
            birthDateError = errors["birthDate"],
            genderError = errors["gender"]
        )

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Error(errors)
    }

    private fun validateContactInfo(data: Map<String, String>): ValidationResult {
        val phoneNumber = data["phoneNumber"] ?: ""
        val email = data["email"] ?: ""

        val phoneNumberResult = validatePhoneNumberUseCase(phoneNumber)
        val emailResult = validateEmailUseCase(email)

        val errors = mutableMapOf<String, UiText>()
        if (phoneNumberResult is Result.Error) errors["phoneNumber"] = phoneNumberResult.error.asUiText()
        if (emailResult is Result.Error) errors["email"] = emailResult.error.asUiText()

        _state.value = _state.value.copy(
            phoneNumber = phoneNumber,
            email = email,
            phoneNumberError = errors["phoneNumber"],
            emailError = errors["email"]
        )

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Error(errors)
    }

    private fun validateCredentials(data: Map<String, String>): ValidationResult {
        val password = data["password"] ?: ""
        val confirmPassword = data["confirmPassword"] ?: ""

        val passwordResult = validatePasswordUseCase(password)
        val confirmPasswordResult = validateConfirmedPasswordUseCase(password, confirmPassword)

        val errors = mutableMapOf<String, UiText>()
        if (passwordResult is Result.Error) errors["password"] = passwordResult.error.asUiText()
        if (confirmPasswordResult is Result.Error) errors["confirmPassword"] = confirmPasswordResult.error.asUiText()

        _state.value = _state.value.copy(
            password = password,
            confirmPassword = confirmPassword,
            passwordError = errors["password"],
            confirmPasswordError = errors["confirmPassword"]
        )

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Error(errors)
    }

    private fun validateFriendCode(data: Map<String, String>): ValidationResult {
        val friendCode = data["friendCode"] ?: ""
        _state.value = _state.value.copy(friendCode = friendCode)
        return ValidationResult.Success
    }

    fun performRegistration(onSuccess: () -> Unit, onError: (UiText) -> Unit) {
        viewModelScope.launch {
            val user = _state.value.toUser()
            when (val result = signUpUseCase(
                email = _state.value.email,
                password = _state.value.password,
                userData = user
            )) {
                is Result.Success -> onSuccess()
                is Result.Error -> onError(result.error.asUiText())
            }
        }
    }

    data class RegistrationFormState(
        val firstName: String = "",
        val lastName: String = "",
        val birthDate: String = "",
        val gender: String = "",
        val allergies: String = "",
        val disabilities: String = "",
        val phoneNumber: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val friendCode: String = "",
        val firstNameError: UiText? = null,
        val lastNameError: UiText? = null,
        val birthDateError: UiText? = null,
        val genderError: UiText? = null,
        val phoneNumberError: UiText? = null,
        val emailError: UiText? = null,
        val passwordError: UiText? = null,
        val confirmPasswordError: UiText? = null
    ) {
        fun toUser() = User(
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate,
            gender = gender,
            allergies = allergies,
            disabilities = disabilities,
            phoneNumber = phoneNumber,
            email = email,
        )
    }
}

enum class RegistrationStep {
    PERSONAL_INFO,
    HEALTH_INFO,
    CONTACT_INFO,
    CREDENTIALS,
    FRIEND_CODE
}