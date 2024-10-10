package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.auth.FirebaseAuthDataSource
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import com.example.hammami.model.User
import com.example.hammami.util.PreferencesManager
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val preferencesManager: PreferencesManager
) {
    private val _authState = MutableStateFlow<Result<User, DataError>>(Result.Error(DataError.Auth.NOT_AUTHENTICATED))
    val authState: StateFlow<Result<User, DataError>> = _authState.asStateFlow()

    suspend fun signIn(email: String, password: String): Result<Unit, DataError> {
        return try {
            Log.d("AuthRepository", "Tentativo di login per: $email")
            val user = firebaseAuthDataSource.signInWithEmailAndPassword(email, password)
            if (user != null) {
                preferencesManager.setLoggedIn(true)
                Log.d("AuthRepository", "Login effettuato con successo per: ${user.uid}")
                Result.Success(Unit)
            } else {
                Log.e("AuthRepository", "Login fallito: utente null")
                Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante il login", e)
            preferencesManager.setLoggedIn(false)
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun signOut(): Result<Unit, DataError> {
        return try {
            Log.d("AuthRepository", "Esecuzione logout")
            firebaseAuthDataSource.signOut()
            preferencesManager.setLoggedIn(false)
            _authState.value = Result.Error(DataError.Auth.NOT_AUTHENTICATED)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante il logout", e)
            Result.Error(DataError.Auth.UNKNOWN)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit, DataError> {
        return try {
            Log.d("AuthRepository", "Tentativo di reset password per: $email")
            firebaseAuthDataSource.resetPassword(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante il reset della password", e)
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }

    fun getCurrentUser() = firebaseAuthDataSource.getCurrentUser()

    suspend fun createUser(email: String, password: String): Result<Unit, DataError> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                Log.e("AuthRepository", "Tentativo di creazione utente con email o password vuota")
                return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }

            Log.d("AuthRepository", "Tentativo di creazione utente per: $email")
            val user = firebaseAuthDataSource.createUser(email, password)
            if (user != null) {
                preferencesManager.setLoggedIn(true)
                Log.d("AuthRepository", "Utente creato con successo: ${user.uid}")
                Result.Success(Unit)
            } else {
                Log.e("AuthRepository", "Creazione utente fallita: utente null")
                Result.Error(DataError.Auth.UNKNOWN)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante la creazione dell'utente", e)
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun updateEmail(email: String): Result<Unit, DataError> {
        return try {
            Log.d("AuthRepository", "Tentativo di aggiornamento email a: $email")
            firebaseAuthDataSource.updateEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante l'aggiornamento dell'email", e)
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun deleteUser(): Result<Unit, DataError> {
        return try {
            Log.d("AuthRepository", "Tentativo di eliminazione account")
            firebaseAuthDataSource.deleteUserAuth()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante l'eliminazione dell'account", e)
            Result.Error(mapAuthExceptionToDataError(e))
        }
    }

    suspend fun refreshAuthToken(): Result<Unit, DataError> {
        return try {
            val user = firebaseAuthDataSource.getCurrentUser()
            if (user != null) {
                Log.d("AuthRepository", "Tentativo di refresh del token per: ${user.uid}")
                val token = user.getIdToken(false).await().token
                if (token != null) {
                    preferencesManager.setLoggedIn(true)
                    Log.d("AuthRepository", "Token refreshato con successo")
                    Result.Success(Unit)
                } else {
                    Log.e("AuthRepository", "Refresh token fallito: token null")
                    handleAuthError(DataError.Auth.TOKEN_REFRESH_FAILED)
                }
            } else {
                Log.e("AuthRepository", "Refresh token fallito: nessun utente corrente")
                handleAuthError(DataError.Auth.NOT_AUTHENTICATED)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Errore durante il refresh del token", e)
            handleAuthError(mapAuthExceptionToDataError(e))
        }
    }

    fun getCurrentUserId(): String? = firebaseAuthDataSource.getCurrentUser()?.uid

    private fun handleAuthError(error: DataError): Result<Unit, DataError> {
        preferencesManager.setLoggedIn(false)
        return Result.Error(error)
    }

    private fun mapAuthExceptionToDataError(e: Exception): DataError {
        return when (e) {
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> DataError.Auth.USER_NOT_FOUND
                "ERROR_WRONG_PASSWORD" -> DataError.Auth.INVALID_CREDENTIALS
                "ERROR_EMAIL_ALREADY_IN_USE" -> DataError.Auth.EMAIL_ALREADY_IN_USE
                "ERROR_WEAK_PASSWORD" -> DataError.Auth.WEAK_PASSWORD
                else -> {
                    Log.e("AuthRepository", "Errore Firebase Auth non gestito: ${e.errorCode}", e)
                    DataError.Auth.UNKNOWN
                }
            }
            else -> {
                Log.e("AuthRepository", "Errore inaspettato", e)
                DataError.Network.UNKNOWN
            }
        }
    }
}