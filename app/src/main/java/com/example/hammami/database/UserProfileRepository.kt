package com.example.hammami.database

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.example.hammami.models.User
import com.example.hammami.util.PreferencesManager
import com.example.hammami.util.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesManager: PreferencesManager,
    private val application: Application
) {
    private val _authState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val authState: StateFlow<Resource<User>> = _authState

    private val _currentUserProfile = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val currentUserProfile = _currentUserProfile.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (preferencesManager.isUserLoggedIn()) {
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


    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user =
                result.user ?: throw Exception("Il login ha avuto successo ma l'utente è null")
            preferencesManager.setLoggedIn(true)
            fetchUserData(user.uid)
            _authState.value
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore durante l'accesso", e)
            preferencesManager.setLoggedIn(false)
            Resource.Error(e.message ?: "Errore sconosciuto durante il login")
        }
    }

    fun signOut() {
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


    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("User not authenticated")

            // Riautenticazione
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).await()

            // Cambio password
            user.updatePassword(newPassword).await()

            Resource.Success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Resource.Error("The current password is incorrect or the authentication has expired.")
        } catch (e: Exception) {
            Resource.Error("Failed to change password: ${e.message}")
        }
    }

    suspend fun refreshUserToken(): Resource<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
            user.getIdToken(true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to refresh user token: ${e.message}")
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

        suspend fun deleteUserProfile(): Resource<Unit> {
            return try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: return Resource.Error("User not authenticated")

                firestore.collection("users").document(userId).delete().await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

//    suspend fun refreshUser() {
//        try {
//            if (isUserSignedIn()) {
//                if (refreshAuthToken()) {
//                    firebaseAuth.currentUser?.let { user ->
//                        fetchUserData(user.uid)
//                    } ?: run {
//                        _authState.value =
//                            Resource.Error("User not authenticated after token refresh")
//                    }
//                } else {
//                    Log.e("UserRepository", "Failed to refresh auth token")
//                    signOut()
//                }
//            } else {
//                Log.d("UserRepository", "User is not signed in")
//                signOut()
//            }
//        } catch (e: Exception) {
//            Log.e("UserRepository", "Error in refreshUser", e)
//            signOut()
//        }
//    }


        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

//    suspend fun updateUserProfile(
//        updatedUser: User,
//        context: Context,
//        currentPassword: String
//    ): Resource<User> {
//        if (!isNetworkAvailable(context)) {
//            return Resource.Error("No internet connection")
//        }
//        return try {
//            val result = performUpdate(updatedUser, currentPassword)
//
//            if (result is Resource.Success) {
//                // Aggiorna _currentUserProfile con i nuovi dati
//                _currentUserProfile.value = result
//                // Aggiorna anche _authState per coerenza
//                _authState.value = result
//            }
//            result
//
//        } catch (e: Exception) {
//            Log.e("UserRepository", "Error updating user data", e)
//            when (e) {
//                is FirebaseFirestoreException -> Resource.Error("Network error: ${e.message}")
//                is FirebaseAuthException -> Resource.Error("Authentication error: ${e.message}")
//                else -> Resource.Error("Unknown error during update: ${e.message}")
//            }
//        }
//    }

        //    suspend fun updateUserProfile(
//        updatedUser: User,
//        context: Context,
//    ): Resource<User> {
//        if (!isNetworkAvailable(context)) {
//            return Resource.Error("No internet connection")
//        }
//        return try {
//            val result = performUpdate(updatedUser)
//
//            if (result is Resource.Success) {
//                // Aggiorna _currentUserProfile con i nuovi dati
//                _currentUserProfile.value = result
//                // Aggiorna anche _authState per coerenza
//                _authState.value = result
//            }
//            result
//
//        } catch (e: Exception) {
//            Log.e("UserRepository", "Error updating user data", e)
//            when (e) {
//                is FirebaseFirestoreException -> Resource.Error("Network error: ${e.message}")
//                else -> Resource.Error("Unknown error during update: ${e.message}")
//            }
//        }
//    }
//    suspend fun updateUserProfile(updatedUser: User, context: Context): Resource<User> {
//        return try {
//            val currentUser = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
//            val uid = currentUser.uid
//
//            firestore.collection("users").document(uid).set(updatedUser).await()
//
//            // Aggiorna i flussi con i nuovi dati
//            val updateResource = Resource.Success(updatedUser)
//            _authState.value = updateResource
//            _currentUserProfile.value = updateResource
//            updateResource
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to update user profile")
//        }
//    }
        suspend fun updateUserProfile(
            updatedUser: User,
            currentPassword: String? = null
        ): Resource<User> {
            if (!isNetworkAvailable(application)) {
                return Resource.Error("No internet connection")
            }
            return try {
                val currentUser =
                    firebaseAuth.currentUser ?: throw Exception("User not authenticated")
                val uid = currentUser.uid

                if (currentPassword != null && updatedUser.email != currentUser.email) {
                    val credential =
                        EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                    currentUser.reauthenticate(credential).await()
                    currentUser.verifyBeforeUpdateEmail(updatedUser.email).await()
                    currentUser.sendEmailVerification().await()
                }

                firestore.collection("users").document(uid).set(updatedUser).await()

                val updateResource = Resource.Success(updatedUser)
                _authState.value = updateResource
                refreshUser()
                updateResource
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to update user profile")
            }
        }

//    private suspend fun performUpdate(updatedUser: User, currentPassword: String): Resource<User> {
//        return try {
//
//
//            val currentUser = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
//            val uid = currentUser.uid
//
//            // Re-authenticate user before sensitive operations
//            val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
//            currentUser.reauthenticate(credential).await()
//
//            // Update email if it has changed
//            if (updatedUser.email != currentUser.email) {
//                currentUser.verifyBeforeUpdateEmail(updatedUser.email).await()
//                currentUser.sendEmailVerification().await()
//            }
//
//            firestore.collection("users").document(uid).set(updatedUser).await()
//
//            // Aggiorno i flussi con i nuovi dati
//            val updateResource = Resource.Success(updatedUser)
//            _authState.value = updateResource
//            _currentUserProfile.value = updateResource
//            refreshUser()
//            updateResource
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to update user profile")
//        }
//
//    }


//    private suspend fun performUpdate(updatedUser: User): Resource<User> {
//        return try {
//
//            val currentUser = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
//            val uid = currentUser.uid
//
//
//            firestore.collection("users").document(uid).set(updatedUser).await()
//
//            // Aggiorno i flussi con i nuovi dati
//            val updateResource = Resource.Success(updatedUser)
//            _authState.value = updateResource
//            _currentUserProfile.value = updateResource
//            refreshUser()
//            updateResource
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to update user profile")
//        }
//    }

//    fun fetchCurrentUserProfile(): Resource<User> {
//        return try {
//            val currentUser = firebaseAuth.currentUser
//            if (currentUser != null) {
//                fetchUserData(currentUser.uid)
//                _currentUserProfile.value
//            } else {
//                Resource.Error("User not authenticated")
//            }
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to fetch current user profile")
//        }
//    }

//    private fun fetchUserData(uid: String) {
//        firestore.collection("users").document(uid).get()
//            .addOnSuccessListener { document ->
//                val user = document.toObject(User::class.java)
//                if (user != null) {
//                    val updatedResource = Resource.Success(user)
//                    _authState.value = updatedResource
//                    _currentUserProfile.value = updatedResource
//                } else {
//                    val errorResource = Resource.Error<User>("User data not found")
//                    _authState.value = errorResource
//                    _currentUserProfile.value = errorResource
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("UserRepository", "Error fetching user data", e)
//                val errorResource = Resource.Error<User>("Failed to fetch user data: ${e.message}")
//                _authState.value = errorResource
//                _currentUserProfile.value = errorResource
//            }
//    }

        fun fetchCurrentUserProfile(): Resource<User> {
            return try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    fetchUserData(currentUser.uid)
                    _authState.value
                } else {
                    Resource.Error("User not authenticated")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to fetch current user profile")
            }
        }

        private fun fetchUserData(uid: String) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        _authState.value = Resource.Success(user)
                    } else {
                        _authState.value = Resource.Error("User data not found")
                    }
                }
                .addOnFailureListener { e ->
                    _authState.value = Resource.Error("Failed to fetch user data: ${e.message}")
                }
        }

        suspend fun refreshUser() {
            try {
                if (firebaseAuth.currentUser != null) {
                    if (firebaseAuth.currentUser?.getIdToken(true)?.await() != null) {
                        firebaseAuth.currentUser?.let { user ->
                            fetchUserData(user.uid)
                        } ?: run {
                            _authState.value =
                                Resource.Error("User not authenticated after token refresh")
                        }
                    } else {
                        _authState.value = Resource.Error("Failed to refresh auth token")
                    }
                } else {
                    _authState.value = Resource.Error("User is not signed in")
                }
            } catch (e: Exception) {
                _authState.value = Resource.Error("Error in refreshUser: ${e.message}")
            }
        }

        suspend fun uploadProfileImage(imageUri: Uri): Resource<String> {
            return try {
                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/profile_images/$filename")
                ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Resource.Success(downloadUrl)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }

        suspend fun updateUserProfileImage(imageUrl: String, user: User): Resource<Unit> {
            return try {
                val currentUser =
                    firebaseAuth.currentUser ?: throw Exception("User not authenticated")
                val uid = currentUser.uid
                val updatedUser = user.copy(profileImage = imageUrl)
                firestore.collection("users").document(uid).set(updatedUser).await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to update user profile")
            }
        }


    }