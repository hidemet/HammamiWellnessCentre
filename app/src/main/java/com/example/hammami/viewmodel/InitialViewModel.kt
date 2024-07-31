package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.models.User
import com.example.hammami.database.UserRepository
import com.example.hammami.util.PreferencesManager
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val authState: StateFlow<Resource<User>> = userRepository.authState


    init {
        viewModelScope.launch {
            if (preferencesManager.isLoggedIn()) {
                userRepository.refreshAuthToken()
            }
        }
    }
}


