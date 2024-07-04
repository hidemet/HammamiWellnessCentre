package com.example.hammami.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.FirebaseDb
import com.example.hammami.model.RegistrationData
import com.example.hammami.model.User
import com.example.hammami.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HammamiViewModel(
    private val firebaseDatabase: FirebaseDb
) : ViewModel() {

    private val _registrationData = MutableLiveData<RegistrationData>()
    val registrationData: LiveData<RegistrationData> = _registrationData

    private val _registrationState = MutableLiveData<Resource<User>>()
    val registrationState: LiveData<Resource<User>> = _registrationState

    private val _loginState = MutableLiveData<Resource<FirebaseUser>>()
    val loginState: LiveData<Resource<FirebaseUser>> = _loginState

    fun updateRegistrationData(update: (RegistrationData) -> RegistrationData) {
        _registrationData.value = update(_registrationData.value ?: RegistrationData())
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            try {
                val result = firebaseDatabase.loginUser(email, password).await()
                _loginState.value = Resource.Success(result.user!!)
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.message ?: "Errore sconosciuto durante il login")
            }
        }
    }

    fun createUser() {
        val user = _registrationData.value?.toUser() ?: return
        val password = _registrationData.value?.password ?: return

        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            try {
                val authResult = firebaseDatabase.createUser(user.email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("UID non disponibile")
                firebaseDatabase.saveUserInformation(uid, user).await()
                _registrationState.value = Resource.Success(user)
            } catch (e: Exception) {
                _registrationState.value = Resource.Error(e.message ?: "Errore sconosciuto durante la registrazione")
            }
        }
    }

    fun clearRegistrationData() {
        _registrationData.value = RegistrationData()
    }
}