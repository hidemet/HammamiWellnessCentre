package com.example.hammami.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.FirebaseDb
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch


class HammamiViewModel(
    private val firebaseDatabase: FirebaseDb

) : ViewModel() {


    // stato di successo o fallimento dell'operazione di login
    val login = MutableLiveData<Boolean>()
    val loginError = MutableLiveData<String>()

    val resetPassword = MutableLiveData<String>()
//    private val _login = MutableSharedFlow<Resource<FirebaseUser>>()
//    val login = _login.asSharedFlow() // converte il mutableshareflow in un immutable shared flow


    fun loginUser(email: String, password: String) =
        firebaseDatabase.loginUser(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                login.postValue(true)
            } else {
                loginError.postValue(it.exception.toString())
            }
        }
}