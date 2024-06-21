package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hammami.model.User
import com.example.hammami.util.Constants.USER_COLLECTION
import com.example.hammami.util.RegisterFieldsState
import com.example.hammami.util.RegisterValidation
import com.example.hammami.util.Resource
import com.example.hammami.util.validateEmail
import com.example.hammami.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth, private val db: FirebaseFirestore) :
    ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(
        Resource.Unspecified()
    )
    var register: Flow<Resource<User>> = _register
    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()
    fun createAccount(user: User, password: String) {
        if (checkValidation(user, password)) {
            _register.value = Resource.Loading()
            firebaseAuth.createUserWithEmailAndPassword(user.email, password).addOnSuccessListener {
                it.user?.let { user ->
                    //_register.value = Resource.Success(user)
                    saveUserInfo(it.uid, user)
                }
            }.addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
        } else {
            val registerFieldsState = RegisterFieldsState(
                user.email.validateEmail(), password.validatePassword())

            runBlocking {_validation.send(registerFieldsState)
        }
    }

/*
        val passwordValidation = checkValidation(user, password)

        passwordValidation is RegisterValidation.Success
        runBlocking { _register.emit(Resource.Loading()) }
        firebaseAuth.createUserWithEmailAndPassword(user.email, password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    _register.value = Resource.Success(user)
                }
            }.addOnFailureListener {
                // Account creation failed
                _register.value = Resource.Error(it.message.toString())
            }*/

    }

    private fun saveUserInfo(userUid: String, user: User) {
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }.addOnFailureListener() {
                _register.value = Resource.Error(it.message.toString())
            }

    }

    private fun checkValidation(user: User, password: String): Boolean {
    return user.email.validateEmail().let { emailValidation ->
        password.validatePassword().let { passwordValidation ->
            emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
        }
    }
}
}