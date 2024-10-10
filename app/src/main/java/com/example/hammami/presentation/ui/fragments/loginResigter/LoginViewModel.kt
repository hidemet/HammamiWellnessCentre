package com.example.hammami.presentation.ui.fragments.loginResigter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.usecase.Error
import com.example.hammami.domain.usecase.ResetPasswordUseCase
import com.example.hammami.domain.usecase.SignInUseCase
import com.example.hammami.domain.usecase.ValidateEmailUseCase
import com.example.hammami.domain.usecase.ValidatePasswordUseCase
import com.example.hammami.domain.usecase.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _uiEvent = MutableStateFlow<UiEvent>(UiEvent.Idle)
    val uiEvent = _uiEvent.asStateFlow()

    sealed class UiEvent {
        object Idle : UiEvent()
        object Loading : UiEvent()
        data class ShowError(val error: UiText) : UiEvent()
        object LoginSuccess : UiEvent()
        data class ResetPasswordSuccess(val email: String) : UiEvent()
        data class ResetPasswordError(val error: UiText) : UiEvent()
    }

    fun onEvent(event: LoginFormEvent) {
        when (event) {
            is LoginFormEvent.EmailChanged -> updateState { copy(email = event.email, emailError = null) }
            is LoginFormEvent.PasswordChanged -> updateState { copy(password = event.password, passwordError = null) }
            is LoginFormEvent.Login -> submitLogin()
            is LoginFormEvent.ResetPassword -> submitResetPassword(event.email)
        }
    }

    private fun submitLogin() {
        val emailResult = validateEmailUseCase(state.value.email)
        val passwordResult = validatePasswordUseCase(state.value.password)

        updateState {
            copy(
                emailError = (emailResult as? Result.Error)?.error?.asUiText(),
                passwordError = (passwordResult as? Result.Error)?.error?.asUiText()
            )
        }

        if (emailResult is Result.Success && passwordResult is Result.Success) {
            performLogin()
        }
    }

    private fun submitResetPassword(email: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)

            when (val emailResult = validateResetPasswordEmail(email)) {
                is Result.Success -> {
                    performResetPassword(email)
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        resetPasswordEmail = email,
                        resetPasswordEmailError = emailResult.error.asUiText()
                    ) }
                    _uiEvent.emit(UiEvent.ResetPasswordError(emailResult.error.asUiText()))
                }
            }

            _uiEvent.emit(UiEvent.Idle)
        }
    }

    fun resetPasswordDialogClosed() {
        _state.update { it.copy(
            resetPasswordEmail = "",
            resetPasswordEmailError = null
        ) }
    }

    private fun performLogin() {
        viewModelScope.launch {
            _uiEvent.value = UiEvent.Loading
            val result = signInUseCase(state.value.email, state.value.password)
            _uiEvent.value = when (result) {
                is Result.Success -> UiEvent.LoginSuccess
                is Result.Error -> UiEvent.ShowError(result.error.asUiText())
            }
        }
    }

    private fun validateResetPasswordEmail(email: String): Result<Unit, Error> {
        return when (val emailResult = validateEmailUseCase(email)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(emailResult.error)
        }
    }

    private suspend fun performResetPassword(email: String) {
        when (val result = resetPasswordUseCase(email)) {
            is Result.Success -> {
                _uiEvent.emit(UiEvent.ResetPasswordSuccess(email))
            }
            is Result.Error -> {
                _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private fun updateState(update: LoginFormState.() -> LoginFormState) {
        _state.update(update)
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
