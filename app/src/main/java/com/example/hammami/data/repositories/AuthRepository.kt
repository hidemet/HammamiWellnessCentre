package com.example.hammami.data.repositories

import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.util.PreferencesManager
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.example.hammami.data.datasource.auth.FirebaseAuthDataSource
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val preferencesManager: PreferencesManager
) {
    private val _authState =
        MutableStateFlow<Result<Unit, DataError>>(Result.Error(DataError.Auth.NOT_AUTHENTICATED))
    val authState = _authState.asStateFlow()

    fun isUserAuthenticated(): Boolean {
        return firebaseAuthDataSource.getCurrentUser() != null
    }

    suspend fun signIn(email: String, password: String): Result<Unit, DataError> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }

            val user = firebaseAuthDataSource.signInWithEmailAndPassword(email, password)
            if (user != null) {
                handleAuthSuccess()
            } else {
                handleAuthError(DataError.Auth.INVALID_CREDENTIALS)
            }
        } catch (e: Exception) {
            handleAuthError(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun signOut(): Result<Unit, DataError> {
        Log.d("AuthRepository", "Starting signOut")
        return try {
            firebaseAuthDataSource.signOut()
            preferencesManager.setLoggedIn(false)
            _authState.value = Result.Success(Unit) // Modifica qui
            Log.d("AuthRepository", "SignOut successful")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignOut error", e)
            Result.Error(DataError.Auth.UNKNOWN)
        }
    }


    suspend fun resetPassword(email: String): Result<Unit, DataError> {
        return try {
            if (email.isBlank()) {
                return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }

            firebaseAuthDataSource.resetPassword(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun createUser(email: String, password: String): Result<Unit, DataError> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }

            val user = firebaseAuthDataSource.createUser(email, password)
            if (user != null) {
                handleAuthSuccess()
            } else {
                handleAuthError(DataError.Auth.UNKNOWN)
            }
        } catch (e: Exception) {
            handleAuthError(mapAuthExceptionToDataError(e))
        }
    }


    fun getCurrentUserId(): Result<String, DataError> {
        return try {
            val uid = firebaseAuthDataSource.getCurrentUser()?.uid
                ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)
            Result.Success(uid)
        } catch (e: Exception) {
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }


    suspend fun updateEmail(newEmail: String): Result<Unit, DataError> {
        return try {
            if (newEmail.isBlank()) {
                return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }

            firebaseAuthDataSource.updateEmail(newEmail)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }


    suspend fun deleteUser(): Result<Unit, DataError> {
        return try {
            firebaseAuthDataSource.deleteUserAuth()
            handleAuthError(DataError.Auth.NOT_AUTHENTICATED)
        } catch (e: Exception) {
            handleAuthError(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun refreshAuthToken(): Result<Unit, DataError> {
        return try {
            val user = firebaseAuthDataSource.getCurrentUser()
            if (user != null) {
                val token = user.getIdToken(false).await().token
                if (token != null) {
                    handleAuthSuccess()
                } else {
                    handleAuthError(DataError.Auth.TOKEN_REFRESH_FAILED)
                }
            } else {
                handleAuthError(DataError.Auth.NOT_AUTHENTICATED)
            }
        } catch (e: Exception) {
            handleAuthError(mapAuthExceptionToDataError(e))
        }
    }

    private fun handleAuthSuccess(): Result<Unit, DataError> {
        preferencesManager.setLoggedIn(true)
        _authState.value = Result.Success(Unit)
        return Result.Success(Unit)
    }

    private fun handleAuthError(error: DataError): Result<Unit, DataError> {
        preferencesManager.setLoggedIn(false)
        _authState.value = Result.Error(error)
        return Result.Error(error)
    }

    private fun mapAuthExceptionToDataError(e: Exception): DataError = when (e) {
        is FirebaseAuthException -> when (e.errorCode) {
            "ERROR_USER_NOT_FOUND" -> DataError.Auth.USER_NOT_FOUND
            "ERROR_WRONG_PASSWORD" -> DataError.Auth.INVALID_CREDENTIALS
            "ERROR_EMAIL_ALREADY_IN_USE" -> DataError.Auth.EMAIL_ALREADY_IN_USE
            "ERROR_WEAK_PASSWORD" -> DataError.Auth.WEAK_PASSWORD
            "ERROR_INVALID_CREDENTIAL" -> DataError.Auth.INVALID_CREDENTIALS
            "ERROR_REQUIRES_RECENT_LOGIN" -> DataError.Auth.NOT_AUTHENTICATED
            else -> DataError.Auth.UNKNOWN
        }

        is FirebaseNetworkException -> DataError.Network.NO_INTERNET
        else -> DataError.Auth.UNKNOWN
    }

}
