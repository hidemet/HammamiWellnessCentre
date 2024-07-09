package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.FirebaseDb
import com.example.hammami.model.RegistrationData
import com.example.hammami.model.User
import com.example.hammami.util.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HammamiViewModel @Inject constructor(
    private val firebaseDb: FirebaseDb
) : ViewModel() {

    private val _registrationData = MutableStateFlow(RegistrationData())
    val registrationData: StateFlow<RegistrationData> = _registrationData

    private val _registrationState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val registrationState: StateFlow<Resource<User>> = _registrationState

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Unspecified())
    val loginState: StateFlow<Resource<FirebaseUser>> = _loginState

    private val _resetPasswordState = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val resetPasswordState: StateFlow<Resource<String>> = _resetPasswordState

    fun updateRegistrationData(update: (RegistrationData) -> RegistrationData) {
        _registrationData.value = update(_registrationData.value)
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            try {
                val result = firebaseDb.loginUser(email, password).await()
                val user = result.user ?: throw Exception("Login successful but user is null")
                _loginState.value = Resource.Success(user)
            } catch (e: Exception) {
                _loginState.value =
                    Resource.Error(e.message ?: "Errore sconosciuto durante il login")
            }
        }
    }

    fun createUser() {
        val user = _registrationData.value.toUser()
        val password = _registrationData.value.password

        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            try {
                val authResult = firebaseDb.createUser(user.email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("UID non disponibile")
                firebaseDb.saveUserInformation(uid, user).await()
                _registrationState.value = Resource.Success(user)
            } catch (e: Exception) {
                _registrationState.value =
                    Resource.Error(e.message ?: "Errore sconosciuto durante la registrazione")
            }
        }
    }

    fun clearRegistrationData() {
        _registrationData.value = RegistrationData()
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            try {
                firebaseDb.resetPassword(email).await()
                _resetPasswordState.value = Resource.Success(email)
            } catch (e: Exception) {
                _resetPasswordState.value =
                    Resource.Error(e.message ?: "Errore durante il reset della password")
            }
        }
    }
}