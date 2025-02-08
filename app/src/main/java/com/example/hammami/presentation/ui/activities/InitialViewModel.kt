package com.example.hammami.presentation.ui.activities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hammami.domain.usecase.user.ObserveUserChangesUseCase


@HiltViewModel
class InitialViewModel @Inject constructor(
    private val observeUserChangesUseCase: ObserveUserChangesUseCase

) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            observeUserChangesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data
                        if (user != null) {
                            Log.d("InitialViewModel", "User: $user")
                            if (user.isadmin) {
                                _navigationEvent.emit(NavigationEvent.NavigateToAdmin)
                            } else {
                                _navigationEvent.emit(NavigationEvent.NavigateToMain)
                            }
                        } else {
                            _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                        }
                    }
                    is Result.Error -> {
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                }
            }
        }
    }

    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
        object NavigateToAdmin : NavigationEvent()
        object NavigateToLogin : NavigationEvent()
    }
}