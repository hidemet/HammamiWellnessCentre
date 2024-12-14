package com.example.hammami.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.user.ObserveUserStateUseCase
import com.example.hammami.domain.usecase.user.RefreshUserStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val observeUserStateUseCase: ObserveUserStateUseCase,
    private val refreshUserStateUseCase: RefreshUserStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() = viewModelScope.launch {
        try {
            refreshUserStateUseCase()
            collectUserState()
        } catch (e: Exception) {
            _uiState.value = UiState.NotLoggedIn
        }
    }

    private suspend fun collectUserState() {
        observeUserStateUseCase()
            .collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> handleSuccessState(result.data)
                    is Result.Error -> handleErrorState(result.error)
                }
            }
    }

    private fun handleSuccessState(user: User?) = when {
        user != null -> UiState.LoggedIn
        else -> UiState.NotLoggedIn
    }

    private fun handleErrorState(error: DataError) = when (error) {
        DataError.Auth.NOT_AUTHENTICATED -> UiState.NotLoggedIn
        else -> UiState.Error(error.asUiText())
    }

    sealed class UiState {
        object Loading : UiState()
        object LoggedIn : UiState()
        object NotLoggedIn : UiState()
        data class Error(val message: UiText) : UiState()
    }
}