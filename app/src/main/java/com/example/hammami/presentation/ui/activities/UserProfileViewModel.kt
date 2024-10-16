package com.example.hammami.presentation.ui.activities

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.usecase.DeleteAccountUseCase
import com.example.hammami.domain.usecase.GetUserUseCase
import com.example.hammami.domain.usecase.ResetPasswordUseCase
import com.example.hammami.domain.usecase.SignOutUseCase
import com.example.hammami.domain.usecase.UpdateUserUseCase
import com.example.hammami.domain.usecase.UploadUserImageUseCase
import com.example.hammami.domain.usecase.ValidateBirthDateUseCase
import com.example.hammami.domain.usecase.ValidateEmailUseCase
import com.example.hammami.domain.usecase.ValidateFirstNameUseCase
import com.example.hammami.domain.usecase.ValidateGenderUseCase
import com.example.hammami.domain.usecase.ValidateLastNameUseCase
import com.example.hammami.domain.usecase.ValidatePhoneNumberUseCase
import com.example.hammami.domain.usecase.Result
import com.example.hammami.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val uploadUserImageUseCase: UploadUserImageUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val validateFirstNameUseCase: ValidateFirstNameUseCase,
    private val validateLastNameUseCase: ValidateLastNameUseCase,
    private val validateBirthDateUseCase: ValidateBirthDateUseCase,
    private val validateGenderUseCase: ValidateGenderUseCase,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            when (val result = getUserUseCase()) {
                is Result.Success -> _userState.value = UserState.LoggedIn(result.data)
                is Result.Error -> _userState.value = UserState.Error(result.error.asUiText())
            }
        }
    }



    fun validateAndUpdatePersonalInfo(personalInfo: PersonalInfo) {
        viewModelScope.launch {
            val validationResults = validatePersonalInfo(personalInfo)
            _validationState.value = validationResults

            if (validationResults.hasErrors()) {
                _uiState.value = UiState.Error(UiText.StringResource(R.string.please_correct_errors))
            } else {
                (userState.value as? UserState.LoggedIn)?.userData?.let { currentUser ->
                    updateUserData(personalInfo.toUser(currentUser))
                }
            }
        }
    }

    fun validateAndUpdateContactInfo(contactInfo: ContactInfo) {
        viewModelScope.launch {
            val validationResults = validateContactInfo(contactInfo)
            _validationState.value = validationResults

            if (validationResults.hasErrors()) {
                _uiState.value = UiState.Error(UiText.StringResource(R.string.please_correct_errors))
            } else {
                (userState.value as? UserState.LoggedIn)?.userData?.let { currentUser ->
                    updateUserData(contactInfo.toUser(currentUser))
                }
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = uploadUserImageUseCase(imageUri)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(UiText.StringResource(R.string.profile_image_updated_successfully))
                    loadUserData() // Fa il refresh dei dati utente
                }
                is Result.Error -> _uiState.value = UiState.Error(result.error.asUiText())
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = resetPasswordUseCase(email)) {
                is Result.Success -> _uiState.value = UiState.Success(UiText.StringResource(R.string.password_reset_email_sent))
                is Result.Error -> _uiState.value = UiState.Error(result.error.asUiText())
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = deleteAccountUseCase()) {
                is Result.Success -> _uiState.value = UiState.AccountDeleted
                is Result.Error -> _uiState.value = UiState.Error(result.error.asUiText())
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = signOutUseCase()) {
                is Result.Success -> _userState.value = UserState.NotLoggedIn
                is Result.Error -> _uiState.value = UiState.Error(result.error.asUiText())
            }
        }
    }

    private fun updateUserData(updatedUser: User) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = updateUserUseCase(updatedUser)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(UiText.StringResource(R.string.profile_updated_successfully))
                    loadUserData()
                    // Resetta lo stato UI con un po' di ritardo
                    delay(100)
                    _uiState.value = UiState.Idle
                }
                is Result.Error -> _uiState.value = UiState.Error(result.error.asUiText())
            }
        }
    }

    private fun validatePersonalInfo(personalInfo: PersonalInfo): ValidationState {
        return ValidationState(
            firstNameError = (validateFirstNameUseCase(personalInfo.firstName) as? Result.Error)?.error?.asUiText(),
            lastNameError = (validateLastNameUseCase(personalInfo.lastName) as? Result.Error)?.error?.asUiText(),
            birthDateError = (validateBirthDateUseCase(personalInfo.birthDate) as? Result.Error)?.error?.asUiText(),
            genderError = (validateGenderUseCase(personalInfo.gender) as? Result.Error)?.error?.asUiText()
        )
    }

    private fun validateContactInfo(contactInfo: ContactInfo): ValidationState {
        return ValidationState(
            phoneNumberError = (validatePhoneNumberUseCase(contactInfo.phoneNumber) as? Result.Error)?.error?.asUiText(),
            emailError = (validateEmailUseCase(contactInfo.email) as? Result.Error)?.error?.asUiText()
        )
    }

    sealed class UserState {
        object Loading : UserState()
        object NotLoggedIn : UserState()
        data class LoggedIn(val userData: User) : UserState()
        data class Error(val message: UiText) : UserState()
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: UiText) : UiState()
        data class Error(val message: UiText) : UiState()
        object AccountDeleted : UiState()
    }

    data class ValidationState(
        val firstNameError: UiText? = null,
        val lastNameError: UiText? = null,
        val birthDateError: UiText? = null,
        val genderError: UiText? = null,
        val phoneNumberError: UiText? = null,
        val emailError: UiText? = null
    ) {
        fun hasErrors() = listOf(firstNameError, lastNameError, birthDateError, genderError, phoneNumberError, emailError).any { it != null }
    }

    data class PersonalInfo(
        val firstName: String,
        val lastName: String,
        val birthDate: String,
        val gender: String,
        val allergies: String,
        val disabilities: String
    ) {
        fun toUser(currentUser: User) = currentUser.copy(
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate,
            gender = gender,
            allergies = allergies,
            disabilities = disabilities
        )
    }

    data class ContactInfo(
        val phoneNumber: String,
        val email: String
    ) {
        fun toUser(currentUser: User) = currentUser.copy(
            phoneNumber = phoneNumber,
            email = email
        )
    }
}