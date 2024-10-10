package com.example.hammami.presentation.ui.fragments.loginResigter

import RegistrationStep
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
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
    val state: StateFlow<RegistrationFormState> = _state.asStateFlow()


    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentStep =
        savedStateHandle.get<RegistrationStep>("currentStep") ?: RegistrationStep.PERSONAL_INFO
        set(value) {
            field = value
            savedStateHandle["currentStep"] = value
        }


    fun updateRegistrationState(update: (RegistrationFormState) -> RegistrationFormState) {
        val updatedState = update(_state.value)
        _state.value = updatedState
        Log.d("RegisterViewModel", "Form data updated: ${_state.value}")
    }

    fun ValidateCurrentStep(step: RegistrationStep) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)
            when (step) {
                RegistrationStep.PERSONAL_INFO -> validatePersonalInfo()
                RegistrationStep.HEALTH_INFO -> validateHealthInfo()
                RegistrationStep.CONTACT_INFO -> validateContactInfo()
                RegistrationStep.CREDENTIALS -> validatePassword()
                RegistrationStep.FRIEND_CODE -> validateFriendCode()
            }
            _uiEvent.emit(UiEvent.Idle)
        }
    }


    private suspend fun validatePersonalInfo() {
        Log.d("RegisterViewModel", "Validazione dati personali: ${_state.value}")
        val firstName = _state.value.firstName
        val lastName = _state.value.lastName

        val firstNameResult = validateFirstNameUseCase(firstName)
        val lastNameResult = validateLastNameUseCase(lastName)

        updateRegistrationState { currentState ->
            currentState.copy(
                firstName = firstName,
                lastName = lastName,
                firstNameError = (firstNameResult as? Result.Error)?.error?.asUiText(),
                lastNameError = (lastNameResult as? Result.Error)?.error?.asUiText()
            )
        }

        if (firstNameResult is Result.Success && lastNameResult is Result.Success) {
            proceedToNextStep()
        } else {
            _uiEvent.emit(UiEvent.ShowError(UiText.DynamicString("Per favore, correggi gli errori prima di continuare")))
        }
    }

    private suspend fun validateHealthInfo() {
        Log.d("RegisterViewModel", "Validazione dati sanitari: ${_state.value}")
        val birthDate = _state.value.birthDate
        val gender = _state.value.gender
        val allergies = _state.value.allergies
        val disabilities = _state.value.disabilities

        val birthDateResult = validateBirthDateUseCase(birthDate)
        val genderResult = validateGenderUseCase(gender)

        updateRegistrationState { currentState ->
            currentState.copy(
                birthDate = birthDate,
                gender = gender,
                allergies = allergies,
                disabilities = disabilities,
                birthDateError = (birthDateResult as? Result.Error)?.error?.asUiText(),
                genderError = (genderResult as? Result.Error)?.error?.asUiText()
            )
        }

        if (birthDateResult is Result.Success && genderResult is Result.Success) {
            proceedToNextStep()
        } else {
            _uiEvent.emit(UiEvent.ShowError(UiText.DynamicString("Per favore, correggi gli errori prima di continuare")))
        }
    }

    private suspend fun validateContactInfo() {
        Log.d("RegisterViewModel", "Validazione dati di contatto: ${_state.value}")
        val phoneNumber = _state.value.phoneNumber
        val email = _state.value.email

        val phoneNumberResult = validatePhoneNumberUseCase(phoneNumber)
        val emailResult = validateEmailUseCase(email)

        updateRegistrationState { currentState ->
            currentState.copy(
                phoneNumber = phoneNumber,
                email = email,
                phoneNumberError = (phoneNumberResult as? Result.Error)?.error?.asUiText(),
                emailError = (emailResult as? Result.Error)?.error?.asUiText()
            )
        }

        if (phoneNumberResult is Result.Success && emailResult is Result.Success) {
            proceedToNextStep()
        } else {
            _uiEvent.emit(UiEvent.ShowError(UiText.DynamicString("Per favore, correggi gli errori prima di continuare")))
        }
    }

    private suspend fun validatePassword() {
        Log.d(
            "RegisterViewModel",
            "Validazione password: ${_state.value.password.replace(Regex("."), "*")}"
        )

        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        val passwordResult = validatePasswordUseCase(password)
        val confirmPasswordResult = validateConfirmedPasswordUseCase(password, confirmPassword)

        updateRegistrationState { currentState ->
            currentState.copy(
                password = password,
                confirmPassword = confirmPassword,
                passwordError = (passwordResult as? Result.Error)?.error?.asUiText(),
                confirmPasswordError = (confirmPasswordResult as? Result.Error)?.error?.asUiText()
            )
        }

        if (passwordResult is Result.Success && confirmPasswordResult is Result.Success) {
            proceedToNextStep()
        } else {
            _uiEvent.emit(UiEvent.ShowError(UiText.DynamicString("Per favore, correggi gli errori prima di continuare")))
        }
    }

    private suspend fun validateFriendCode() {
        Log.d("RegisterViewModel", "Validazione codice amico: ${_state.value.friendCode}")

        val friendCode = _state.value.friendCode
        updateRegistrationState { currentState ->
            currentState.copy(friendCode = friendCode)
        }
        performRegistration()
    }

    private suspend fun performRegistration() {

        Log.d("RegisterViewModel", "Inizio processo di registrazione")

        // Logga tutti i dati del form
        with(_state.value) {
            Log.d(
                "RegisterViewModel", """
            Dati di registrazione:
            Nome: $firstName
            Cognome: $lastName
            Data di nascita: $birthDate
            Genere: $gender
            Allergie: $allergies
            Disabilità: $disabilities
            Numero di telefono: $phoneNumber
            Email: $email
            Password: ${password.replace(Regex("."), "*")} // Nascondi la password nei log
            Codice amico: $friendCode
        """.trimIndent()
            )
        }

        val user = _state.value.toUser()
        Log.d("RegisterViewModel", "Oggetto User creato: $user")

        val result = signUpUseCase(
            email = _state.value.email,
            password = _state.value.password,
            userData = user
        )



        when (result) {
            is Result.Success -> {
                Log.d("RegisterViewModel", "Registrazione completata con successo")

                _uiEvent.emit(UiEvent.RegistrationSuccess)

            }

            is Result.Error -> {
                Log.e("RegisterViewModel", "Errore durante la registrazione: ${result.error}")
                _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private suspend fun proceedToNextStep() {
        currentStep = when (currentStep) {
            RegistrationStep.PERSONAL_INFO -> RegistrationStep.HEALTH_INFO
            RegistrationStep.HEALTH_INFO -> RegistrationStep.CONTACT_INFO
            RegistrationStep.CONTACT_INFO -> RegistrationStep.CREDENTIALS
            RegistrationStep.CREDENTIALS -> RegistrationStep.FRIEND_CODE
            RegistrationStep.FRIEND_CODE -> {
                performRegistration()
                return
            }

        }
        _uiEvent.emit(UiEvent.NavigateToNextStep(currentStep))
    }


    sealed class UiEvent {
        object Idle : UiEvent()
        object Loading : UiEvent()
        data class NavigateToNextStep(val nextStep: RegistrationStep) : UiEvent()
        object RegistrationSuccess : UiEvent()
        data class ShowError(val error: UiText) : UiEvent()
    }
}