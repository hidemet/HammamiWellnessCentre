package com.example.hammami.database

import android.util.Log
import com.example.hammami.models.User
import com.example.hammami.util.PreferencesManager
import com.example.hammami.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesManager: PreferencesManager
) {
    private val _authState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val authState: StateFlow<Resource<User>> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (preferencesManager.isLoggedIn()) {
            firebaseAuth.currentUser?.let { user ->
                fetchUserData(user.uid)
            } ?: run {
                _authState.value = Resource.Error("Utente non autenticato")
                preferencesManager.setLoggedIn(false)
            }
        } else {
            _authState.value = Resource.Error("Utente non registrato")
        }

        firebaseAuth.addAuthStateListener { auth ->
            auth.currentUser?.let { user ->
                fetchUserData(user.uid)
            } ?: run {
                _authState.value = Resource.Error("Utente non autenticato")
                preferencesManager.setLoggedIn(false)
            }
        }
    }

    private fun fetchUserData(uid: String) {
        if(firebaseAuth.currentUser == null) {
            _authState.value = Resource.Error("Utente non autenticato")
            return
        }
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _authState.value = Resource.Success(user)
                } else {
                    _authState.value = Resource.Error("Dati utente non trovati")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Errore nel recupero dei dati utente", e)
                _authState.value = Resource.Error("Fallimento nel recupero dei dati utente: ${e.message}")
            }
    }

    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Il login ha avuto successo ma l'utente è null")
            preferencesManager.setLoggedIn(true)
            fetchUserData(user.uid)
            _authState.value
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore durante l'accesso", e)
            preferencesManager.setLoggedIn(false)
            Resource.Error(e.message ?: "Errore sconosciuto durante il login")
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        preferencesManager.setLoggedIn(false)
        _authState.value = Resource.Error("L'utente è stato disconnesso")
    }

    suspend fun signUp(email: String, password: String, userData: User): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign up successful but user is null")
            saveUserData(user.uid, userData)
            preferencesManager.setLoggedIn(true)
            Resource.Success(userData)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error during sign up", e)
            Resource.Error(e.message ?: "Unknown error during registration")
        }
    }

    private suspend fun saveUserData(uid: String, userData: User) {
        firestore.collection("users").document(uid).set(userData).await()
    }

    fun isUserSignedIn() = firebaseAuth.currentUser != null

    suspend fun refreshAuthToken(): Boolean {
        return try {
            firebaseAuth.currentUser?.getIdToken(true)?.await() != null
        } catch (e: Exception) {
            Log.e("UserRepository", "Error refreshing auth token", e)
            false
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sending password reset email", e)
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    suspend fun refreshUser() {
        try {
            if (isUserSignedIn()) {
                if (refreshAuthToken()) {
                    firebaseAuth.currentUser?.let { user ->
                        fetchUserData(user.uid)
                    } ?: run {
                        _authState.value = Resource.Error("User not authenticated after token refresh")
                    }
                } else {
                    Log.e("UserRepository", "Failed to refresh auth token")
                    signOut()
                }
            } else {
                Log.d("UserRepository", "User is not signed in")
                signOut()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error in refreshUser", e)
            signOut()
        }
    }

    suspend fun updateUserData(updatedUser: User): Resource<User> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("User not authenticated")
            firestore.collection("users").document(uid).set(updatedUser).await()
            Resource.Success(updatedUser)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user data", e)
            Resource.Error(e.message ?: "Unknown error during update")
        }
    }

     fun getCurrentUser(): Resource<User> {
        return _authState.value
    }
}