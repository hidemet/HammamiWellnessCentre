package com.example.hammami.presentation.ui.activities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.AuthRepository // Importa AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.domain.usecase.user.ObserveUserChangesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository // Usa AuthRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            if (authRepository.isUserAuthenticated()) {
                val userIdResult = authRepository.getCurrentUserId()
                when (userIdResult) {
                    is Result.Success -> {
                        val isAdmin = userRepository.isUserAdmin(userIdResult.data)
                        if (isAdmin is Result.Success) {
                            _navigationEvent.emit(if (isAdmin.data) NavigationEvent.NavigateToAdmin else NavigationEvent.NavigateToMain)
                        } else {
                            _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                        }
                    }
                    is Result.Error -> {
                        Log.e("InitialViewModel", "Error getting user ID: ${userIdResult.error}")
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                }
            } else {
                // L'utente non è autenticato
                Log.d("InitialViewModel", "User is not authenticated, navigating to login")
                _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            }
        }
    }


    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
        object NavigateToAdmin : NavigationEvent()
        object NavigateToLogin : NavigationEvent()
    }
}