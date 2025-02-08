package com.example.hammami.presentation.ui.features.loginResigter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.error.Error
import com.example.hammami.domain.usecase.auth.ResetPasswordUseCase
import com.example.hammami.domain.usecase.auth.SignInUseCase
import com.example.hammami.domain.usecase.validation.account.ValidateEmailUseCase
import com.example.hammami.domain.usecase.validation.account.ValidatePasswordUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.auth.IsAdminUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val isAdminUseCase: IsAdminUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun updateEmailField(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun updatePasswordField(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun submitLogin() = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        if (!validateLoginFields()) {
            emitUiEvent(UiEvent.ShowError(UiText.StringResource((R.string.error_invalid_credentials))))
            return@launch
        }

        when (val resultSignIn = signInUseCase(state.value.email, state.value.password)) {
            is Result.Success -> {
                when (val isAdminResult = isAdminUseCase()) {
                    is Result.Success -> {
                        if (isAdminResult.data) {
                            emitUiEvent(UiEvent.NavigateToAdminActivity)
                        } else {
                            emitUiEvent(UiEvent.NavigateToMainActivity)
                        }
                    }

                    is Result.Error -> emitUiEvent(UiEvent.ShowError(isAdminResult.error.asUiText()))
                }
            }

            is Result.Error -> emitUiEvent(UiEvent.ShowError(resultSignIn.error.asUiText()))
        }

    }

    private fun validateLoginFields(): Boolean {
        val emailResult = validateEmailUseCase(state.value.email)
        val passwordResult = validatePasswordUseCase(state.value.password)

        val hasErrors = updateValidationErrors(emailResult, passwordResult)
        return !hasErrors
    }

    private fun updateValidationErrors(
        emailResult: Result<Unit, Error>,
        passwordResult: Result<Unit, Error>
    ): Boolean {
        val emailError = (emailResult as? Result.Error)?.error?.asUiText()
        val passwordError = (passwordResult as? Result.Error)?.error?.asUiText()

        _state.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }

        return emailError != null || passwordError != null
    }


    fun handlePasswordReset(email: String) = viewModelScope.launch {
        updateUiState { copy(isLoading = true) }
        when (val validationResult = validateEmailUseCase(email)) {
            is Result.Success -> executePasswordReset(email)
            is Result.Error -> handleResetPasswordValidationError(email, validationResult.error)
        }
    }

    private suspend fun executePasswordReset(email: String) {
        when (val result = resetPasswordUseCase(email)) {
            is Result.Success -> emitUiEvent(UiEvent.ResetPasswordSuccess(email))
            is Result.Error -> emitUiEvent(UiEvent.ResetPasswordError(result.error.asUiText()))
        }
    }

    private suspend fun handleResetPasswordValidationError(email: String, error: Error) {
        _state.update {
            it.copy(
                resetPasswordEmail = email,
                resetPasswordEmailError = error.asUiText()
            )
        }
        emitUiEvent(UiEvent.ResetPasswordError(error.asUiText()))
    }

    fun resetPasswordDialogClosed() {
        _state.update {
            it.copy(
                resetPasswordEmail = "",
                resetPasswordEmailError = null
            )
        }
    }

    private suspend fun emitUiEvent(event: UiEvent) {
        updateUiState { copy(isLoading = false) }
        _uiEvent.emit(event)
    }

    private fun updateUiState(update: UiState.() -> UiState) {
        _state.update(update)
    }


    sealed class UiEvent {
        data class ShowError(val error: UiText) : UiEvent()
        object NavigateToMainActivity : UiEvent()
        object NavigateToAdminActivity : UiEvent()
        data class ResetPasswordSuccess(val email: String) : UiEvent()
        data class ResetPasswordError(val error: UiText) : UiEvent()
    }

    data class UiState(
        val isLoading: Boolean = false,
        val email: String = "",
        val emailError: UiText? = null,
        val resetPasswordEmail: String = "",
        val resetPasswordEmailError: UiText? = null,
        val password: String = "",
        val passwordError: UiText? = null
    )
}
