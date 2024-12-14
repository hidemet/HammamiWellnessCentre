package com.example.hammami.presentation.ui.activities

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.User
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.usecase.auth.DeleteAccountUseCase
import com.example.hammami.domain.usecase.auth.ResetPasswordUseCase
import com.example.hammami.domain.usecase.auth.SignOutUseCase
import com.example.hammami.domain.usecase.user.UpdateUserUseCase
import com.example.hammami.domain.usecase.user.UploadUserImageUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateBirthDateUseCase
import com.example.hammami.domain.usecase.validation.account.ValidateEmailUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateFirstNameUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateGenderUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateLastNameUseCase
import com.example.hammami.domain.usecase.validation.user.ValidatePhoneNumberUseCase
import com.example.hammami.domain.usecase.user.ObserveUserStateUseCase
import com.example.hammami.domain.usecase.user.RefreshUserStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val updateUserUseCase: UpdateUserUseCase,
    private val uploadUserImageUseCase: UploadUserImageUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val validationUseCases: ValidationUseCases,
    private val observeUserStateUseCase: ObserveUserStateUseCase,
    private val refreshUserStateUseCase: RefreshUserStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        initializeUserState()
    }

    fun onEvent(event: UserProfileEvent) = viewModelScope.launch {
        when (event) {
            is UserProfileEvent.LoadUserData -> refreshUserData()
            is UserProfileEvent.UpdateUserInfo -> handleUpdateUserInfo(event.userInfo)
            is UserProfileEvent.UploadProfileImage -> handleUploadProfileImage(event.imageUri)
            is UserProfileEvent.ResetPassword -> handleResetPassword(event.email)
            is UserProfileEvent.DeleteAccount -> handleDeleteAccount()
            is UserProfileEvent.SignOut -> handleSignOut()
        }
    }

    private fun initializeUserState() {
        observeUserState()
        refreshUserData()
    }

    private fun observeUserState() {
        viewModelScope.launch {
            observeUserStateUseCase()
                .collect { result -> handleUserStateResult(result) }
        }
    }

    private suspend fun handleUserStateResult(result: Result<User?, DataError>) {
        when (result) {
            is Result.Success -> updateUserState(result.data)
            is Result.Error -> handleUserStateError(result.error)
        }
    }

    private fun updateUserState(user: User?) {
        _uiState.update { it.copy(user = user) }
    }

    private suspend fun handleUserStateError(error: DataError) {
        _uiState.update { it.copy(user = null) }
        emitError(error.asUiText())
    }

    private suspend fun handleUpdateUserInfo(userInfo: UserInfo) {
        val validationResult = validateUserInfo(userInfo)
        if (validationResult.hasErrors()) {
            handleValidationErrors(validationResult)
            return
        }
        executeUpdateUser(userInfo)
    }

    private suspend fun executeUpdateUser(userInfo: UserInfo) {
        setLoading(true)
        when (val result = updateUserUseCase(userInfo.toUser(requireCurrentUser()))) {
            is Result.Success -> handleUpdateSuccess()
            is Result.Error -> handleError(result.error.asUiText())
        }
    }

    private suspend fun handleUploadProfileImage(imageUri: Uri) {
        setLoading(true)
        when (val result = uploadUserImageUseCase(imageUri)) {
            is Result.Success -> handleImageUploadSuccess()
            is Result.Error -> handleError(result.error.asUiText())
        }
    }

    private suspend fun handleResetPassword(email: String) {
        setLoading(true)
        when (val result = resetPasswordUseCase(email)) {
            is Result.Success -> emitEvent(UiEvent.ShowSnackbar(UiText.StringResource(R.string.password_reset_email_sent)))
            is Result.Error -> handleError(result.error.asUiText())
        }
    }

    private suspend fun handleDeleteAccount() {
        setLoading(true)
        when (val result = deleteAccountUseCase()) {
            is Result.Success -> emitEvent(UiEvent.AccountDeleted)
            is Result.Error -> handleError(result.error.asUiText())
        }
    }

    private suspend fun handleSignOut() {
        setLoading(true)
        try {
            when (val result = signOutUseCase()) {
                is Result.Success -> handleSignOutSuccess()
                is Result.Error -> handleError(result.error.asUiText())
            }
        } catch (e: Exception) {
            handleError(UiText.DynamicString(e.message ?: "Unknown error"))
        }
    }

    private suspend fun handleSignOutSuccess() {
        _uiState.update { it.copy(user = null) }
        emitEvent(UiEvent.SignOut)
    }

    private fun validateUserInfo(userInfo: UserInfo): ValidationState {
        return ValidationState(
            firstNameError = (validationUseCases.validateFirstName(userInfo.firstName) as? Result.Error)?.error?.asUiText(),
            lastNameError = (validationUseCases.validateLastName(userInfo.lastName) as? Result.Error)?.error?.asUiText(),
            birthDateError = (validationUseCases.validateBirthDate(userInfo.birthDate) as? Result.Error)?.error?.asUiText(),
            genderError = (validationUseCases.validateGender(userInfo.gender) as? Result.Error)?.error?.asUiText(),
            phoneNumberError = (validationUseCases.validatePhoneNumber(userInfo.phoneNumber) as? Result.Error)?.error?.asUiText(),
            emailError = (validationUseCases.validateEmail(userInfo.email) as? Result.Error)?.error?.asUiText()
        )
    }

    private suspend fun handleValidationErrors(validationState: ValidationState) {
        _uiState.update { it.copy(validationState = validationState) }
        emitEvent(UiEvent.ShowSnackbar(UiText.StringResource(R.string.please_correct_errors)))
    }

    private suspend fun handleUpdateSuccess() {
        emitEvent(UiEvent.ShowSnackbar(UiText.StringResource(R.string.profile_updated_successfully)))
        refreshUserData()
    }

    private suspend fun handleImageUploadSuccess() {
        emitEvent(UiEvent.ShowSnackbar(UiText.StringResource(R.string.profile_image_updated_successfully)))
        refreshUserData()
    }

    private fun refreshUserData() = viewModelScope.launch {
        setLoading(true)
        refreshUserStateUseCase()
        setLoading(false)
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun requireCurrentUser(): User =
        uiState.value.user ?: throw IllegalStateException("User not found")

    private suspend fun emitEvent(event: UiEvent) {
        _events.emit(event)
    }
    private suspend fun emitError(error: UiText) {
        viewModelScope.launch {  // Aggiungi questo per risolvere l'errore suspend
            setLoading(false)
            emitEvent(UiEvent.ShowSnackbar(error))
        }
    }

    data class ValidationUseCases @Inject constructor(
        val validateFirstName: ValidateFirstNameUseCase,
        val validateLastName: ValidateLastNameUseCase,
        val validateBirthDate: ValidateBirthDateUseCase,
        val validateGender: ValidateGenderUseCase,
        val validatePhoneNumber: ValidatePhoneNumberUseCase,
        val validateEmail: ValidateEmailUseCase
    )

    private suspend fun handleError(error: UiText) {
        _uiState.update { it.copy(isLoading = false) }
        _events.emit(UiEvent.ShowSnackbar(error))
    }

    data class UiState(
        val user: User? = null,
        val isLoading: Boolean = false,
        val validationState: ValidationState = ValidationState()
    )

    sealed class UiEvent {
        data class ShowSnackbar(val message: UiText) : UiEvent()
        object AccountDeleted : UiEvent()
        object SignOut : UiEvent()
    }

    sealed class UserProfileEvent {
        object LoadUserData : UserProfileEvent()
        data class UpdateUserInfo(val userInfo: UserInfo) : UserProfileEvent()
        data class UploadProfileImage(val imageUri: Uri) : UserProfileEvent()
        data class ResetPassword(val email: String) : UserProfileEvent()
        object DeleteAccount : UserProfileEvent()
        object SignOut : UserProfileEvent()
    }

    data class ValidationState(
        val firstNameError: UiText? = null,
        val lastNameError: UiText? = null,
        val birthDateError: UiText? = null,
        val genderError: UiText? = null,
        val phoneNumberError: UiText? = null,
        val emailError: UiText? = null
    ) {
        fun hasErrors() = listOf(
            firstNameError,
            lastNameError,
            birthDateError,
            genderError,
            phoneNumberError,
            emailError
        ).any { it != null }
    }

    data class UserInfo(
        val firstName: String,
        val lastName: String,
        val birthDate: String,
        val gender: String,
        val allergies: String,
        val disabilities: String,
        val phoneNumber: String,
        val email: String
    ) {
        fun toUser(currentUser: User) = currentUser.copy(
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate,
            gender = gender,
            allergies = allergies,
            disabilities = disabilities,
            phoneNumber = phoneNumber,
            email = email
        )
    }
}