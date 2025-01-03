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
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginFormState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: LoginFormEvent) = viewModelScope.launch {
        when (event) {
            is LoginFormEvent.EmailChanged -> updateEmailField(event.email)
            is LoginFormEvent.PasswordChanged -> updatePasswordField(event.password)
            LoginFormEvent.Login -> handleLogin()
            is LoginFormEvent.ResetPassword -> handlePasswordReset(event.email)
        }
    }

    private fun updateEmailField(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    private fun updatePasswordField(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    private suspend fun handleLogin() {
        if (!validateLoginFields()) return
        executeLogin()
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

        _state.update { it.copy(
            emailError = emailError,
            passwordError = passwordError
        )}

        return emailError != null || passwordError != null
    }

    private suspend fun executeLogin() {
        emitUiEvent(UiEvent.Loading)
        try {
            when (val result = signInUseCase(state.value.email, state.value.password)) {
                is Result.Success -> emitUiEvent(UiEvent.LoginSuccess)
                is Result.Error -> handleLoginError(result.error)
            }
        } catch (e: Exception) {
            handleUnexpectedError()
        }
    }

    private suspend fun handleLoginError(error: Error) {
        emitUiEvent(UiEvent.ShowError(error.asUiText()))
        emitUiEvent(UiEvent.Idle)
    }

    private suspend fun handleUnexpectedError() {
        emitUiEvent(UiEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
        emitUiEvent(UiEvent.Idle)
    }

    private suspend fun handlePasswordReset(email: String) {
        emitUiEvent(UiEvent.Loading)

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
        emitUiEvent(UiEvent.Idle)
    }

    private suspend fun handleResetPasswordValidationError(email: String, error: Error) {
        _state.update { it.copy(
            resetPasswordEmail = email,
            resetPasswordEmailError = error.asUiText()
        )}
        emitUiEvent(UiEvent.ResetPasswordError(error.asUiText()))
        emitUiEvent(UiEvent.Idle)
    }

    fun resetPasswordDialogClosed() {
        _state.update { it.copy(
            resetPasswordEmail = "",
            resetPasswordEmailError = null
        )}
    }

    private suspend fun emitUiEvent(event: UiEvent) {
        _uiEvent.emit(event)
    }


    sealed class UiEvent {
        object Idle : UiEvent()
        object Loading : UiEvent()
        data class ShowError(val error: UiText) : UiEvent()
        object LoginSuccess : UiEvent()
        data class ResetPasswordSuccess(val email: String) : UiEvent()
        data class ResetPasswordError(val error: UiText) : UiEvent()
    }

    data class LoginFormState(
        val email: String = "",
        val emailError: UiText? = null,
        val resetPasswordEmail: String = "",
        val resetPasswordEmailError: UiText? = null,
        val password: String = "",
        val passwordError: UiText? = null
    )


    sealed class LoginFormEvent {
        data class EmailChanged(val email: String) : LoginFormEvent()
        data class PasswordChanged(val password: String) : LoginFormEvent()
        object Login : LoginFormEvent()
        data class ResetPassword(val email: String) : LoginFormEvent()
    }
}
