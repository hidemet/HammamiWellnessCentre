package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.models.RegistrationData
import com.example.hammami.models.User
import com.example.hammami.database.UserRepository
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginRegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _registrationData = MutableStateFlow(RegistrationData())
    val registrationData: StateFlow<RegistrationData> = _registrationData

    private val _registrationState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val registrationState: StateFlow<Resource<User>> = _registrationState

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val loginState: StateFlow<Resource<User>> = _loginState

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>>(Resource.Unspecified())
    val resetPasswordState: StateFlow<Resource<Unit>> = _resetPasswordState

    fun updateRegistrationData(update: (RegistrationData) -> RegistrationData) {
        _registrationData.value = update(_registrationData.value)
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = userRepository.signIn(email, password)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            userRepository.signOut()
            _loginState.value = Resource.Unspecified()
        }
    }

    fun createUser(email: String, password: String, userData: User) {
        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            _registrationState.value = userRepository.signUp(email, password, userData)
        }
    }

    fun clearRegistrationData() {
        _registrationData.value = RegistrationData()
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            _resetPasswordState.value = userRepository.resetPassword(email)
        }
    }
}