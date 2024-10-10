package com.example.hammami.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.domain.usecase.RefreshAuthAndUserDataUseCase
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import com.example.hammami.model.User
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val refreshAuthAndUserDataUseCase: RefreshAuthAndUserDataUseCase,
    authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<Result<User, DataError>> = authRepository.authState

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        refreshAuthToken()
    }

    private fun refreshAuthToken() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = refreshAuthAndUserDataUseCase()) {
                is Result.Success -> {
                    _uiState.value = UiState.LoggedIn
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.error.asUiText())
                }
            }
        }
    }

    sealed class UiState {
        object Loading : UiState()
        object LoggedIn : UiState()
        object NotLoggedIn : UiState()
        data class Error(val message: UiText) : UiState()
    }
}