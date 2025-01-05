package com.example.hammami.presentation.ui.features.userProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.auth.DeleteAccountUseCase
import com.example.hammami.domain.usecase.auth.ResetPasswordUseCase
import com.example.hammami.domain.usecase.auth.SignOutUseCase
import com.example.hammami.domain.usecase.user.ObserveUserChangesUseCase
import com.example.hammami.domain.usecase.user.UpdateUserUseCase
import com.example.hammami.domain.usecase.user.UploadUserImageUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val observeUserChangesUseCase: ObserveUserChangesUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val uploadUserImageUseCase: UploadUserImageUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val validateUserUseCase: ValidateUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    init {
        observeUserChanges()
    }

    private fun observeUserChanges() {
        viewModelScope.launch {
            observeUserChangesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> updateUiState {
                        copy(
                            user = result.data,
                            isLoading = false
                        )
                    }

                    is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
                }
            }
        }
    }


    fun signOut() = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        when (val result = signOutUseCase()) {
            is Result.Success -> emitEvent(UiEvent.NavigateToLogin)
            is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        when (val result = deleteAccountUseCase()) {
            is Result.Success -> emitEvent(UiEvent.NavigateToLogin)
            is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
        }
    }


    fun updateUserData(info: UserData, userPassword: String? = null) = viewModelScope.launch {
        val currentUser = uiState.value.user ?: return@launch
        val updatedUser = info.toUser(currentUser)
        val emailChanged = currentUser.email != updatedUser.email
        updateUiState { copy(isLoading = true) }

        // Validiamo i dati utente
        val validationResult = validateUserUseCase(updatedUser)
        if (validationResult.hasErrors()) {
            val validationState = ValidationState.fromValidationResult(validationResult)
            updateUiState {
                copy(userValidationError = validationState)
            }
            return@launch
        }

        when (val result = updateUserUseCase(updatedUser, emailChanged, userPassword)) {
            is Result.Success -> {
                 emitEvent(UiEvent.UpdateUserSuccess(UiText.StringResource(R.string.profile_updated_successfully)))
            }

            is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
        }
    }


    fun uploadProfileImage(imageUri: Uri) = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        when (val result = uploadUserImageUseCase(imageUri)) {
            is Result.Success -> {
                emitEvent(UiEvent.UserMessage(UiText.StringResource(R.string.profile_image_updated_successfully)))
            }

            is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
        }
    }


    fun resetPassword(email: String) = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        when (val result = resetPasswordUseCase(email)) {
            is Result.Success -> emitEvent(UiEvent.UserMessage(UiText.StringResource(R.string.password_reset_email_sent)))
            is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
        }
    }

    fun deleteAccount(email: String) = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        when (val result = deleteAccountUseCase()) {
            is Result.Success -> emitEvent(UiEvent.NavigateToLogin)
            is Result.Error -> emitEvent(UiEvent.UserMessage(result.error.asUiText()))
        }
    }



    private fun updateUiState(update: UiState.() -> UiState) {
        _uiState.update(update)
    }


    private suspend fun emitEvent(event: UiEvent) {
        updateUiState { copy(isLoading = false) }
        _uiEvents.emit(event)
    }

    data class ValidationState(
        val firstNameError: UiText? = null,
        val lastNameError: UiText? = null,
        val birthDateError: UiText? = null,
        val phoneNumberError: UiText? = null,
        val emailError: UiText? = null
    ) {
        companion object {
            fun fromValidationResult(result: ValidateUserUseCase.ValidationResult): ValidationState {
                return ValidationState(
                    firstNameError = result.firstNameError?.asUiText(),
                    lastNameError = result.lastNameError?.asUiText(),
                    birthDateError = result.birthDateError?.asUiText(),
                    phoneNumberError = result.phoneNumberError?.asUiText(),
                    emailError = result.emailError?.asUiText()
                )
            }
        }
    }

    data class UiState(
        val user: User? = null,
        val userValidationError: ValidationState? = null,
        val isLoading: Boolean = false,
    )


    sealed class UiEvent {
        data class UserMessage(val message: UiText) : UiEvent() {
            companion object {
                fun error(message: UiText) = UserMessage(message)
            }
        }
        object NavigateToLogin : UiEvent()
        data class UpdateUserSuccess(val message: UiText) : UiEvent()
    }

    sealed class UserData {
        abstract fun toUser(currentUser: User): User

        data class PersonalInfoData(
            val firstName: String,
            val lastName: String,
            val birthDate: String,
            val gender: String,
            val allergies: String,
            val disabilities: String
        ) : UserData() {
            override fun toUser(currentUser: User) = currentUser.copy(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
                gender = gender,
                allergies = allergies,
                disabilities = disabilities
            )
        }

        data class ContactInfoData(
            val email: String,
            val phoneNumber: String
        ) : UserData() {
            override fun toUser(currentUser: User) = currentUser.copy(
                email = email,
                phoneNumber = phoneNumber
            )
        }
    }
}