package com.example.hammami.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hammami.database.FirebaseDb
import com.example.hammami.model.User
import com.example.hammami.util.Resource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase




class HammamiViewModel(
    private val firebaseDatabase: FirebaseDb

) : ViewModel() {


    // stato di successo o fallimento dell'operazione di login
    val login = MutableLiveData<Boolean>()
    val loginError = MutableLiveData<String>()

    private val registerUserDataMap = mutableMapOf<String, Any>()

    val register = MutableLiveData<Resource<User>>()

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
    fun updateRegisterUserData(key: String, value: Any) {
        registerUserDataMap[key] = value
    }

    fun clearRegisterUserData() {
        registerUserDataMap.clear()
    }
    fun createUser(user: User, password: String, ) {
        firebaseDatabase.createUser(user.email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseDatabase.saveUserInformation(Firebase.auth.currentUser!!.uid, user).addOnCompleteListener {it2 ->
                    if (it2.isSuccessful) {
                        register.postValue(Resource.Success(user))
                    } else {
                        register.postValue(Resource.Error(it2.exception.toString(), null))
                    }
                }
            } else
                register.postValue(Resource.Error(it.exception.toString()))

        }
    }
}