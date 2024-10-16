package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.user.FirebaseFirestoreUserDataSource
import com.example.hammami.data.datasource.user.FirebaseStorageUserDataSource
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import com.example.hammami.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.Flow
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val storageDataSource: FirebaseStorageUserDataSource,
    private val authRepository: AuthRepository
) {
    suspend fun getUserData(): Result<User, DataError> {
        return try {
            val uid = authRepository.getCurrentUserId() ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)
            val user = firestoreDataSource.fetchUserData(uid)
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel recupero dei dati utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }



    suspend fun getUserData(userId: String?): Result<User, DataError> {
        return try {
            if (userId == null) {
                return Result.Error(DataError.User.USER_NOT_FOUND)
            }
            val userData = firestoreDataSource.fetchUserData(userId)
            if (userData != null) {
                Result.Success(userData)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel recupero dei dati utente per ID: $userId", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun signUp(email: String, password: String, userData: User): Result<User, DataError> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
        }
        return when (val authResult = authRepository.createUser(email, password)) {
            is Result.Success -> {
                val uid = authRepository.getCurrentUserId()
                    ?: return Result.Error(DataError.Auth.UNKNOWN)
                when (val saveResult = saveUser(uid, userData)) {
                    is Result.Success -> Result.Success(userData.copy(email = email))
                    is Result.Error -> {
                        // Rollback: elimina l'account appena creato se il salvataggio del profilo fallisce
                        authRepository.deleteUser()
                        Result.Error(saveResult.error)
                    }
                }
            }

            is Result.Error -> Result.Error(authResult.error)
        }
    }

    suspend fun updateUser(user: User): Result<Unit, DataError> {
        return try {
            val uid = authRepository.getCurrentUserId()
                ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)

            firestoreDataSource.updateUser(uid, user)
            if (user.email != authRepository.getCurrentUser()?.email) {
                authRepository.updateEmail(user.email)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nell'aggiornamento del profilo utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

     suspend fun saveUser(userUid: String, user: User): Result<Unit, DataError> {
        return try {
            firestoreDataSource.saveUserInformation(userUid, user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel salvataggio del profilo utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun uploadUserImage(imageUri: Uri): Result<String, DataError> {
        return try {
            val downloadUrl = storageDataSource.uploadUserImage(imageUri)
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel caricamento dell'immagine del profilo", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun deleteUser(): Result<Unit, DataError> {
        return try {
            val uid = authRepository.getCurrentUserId()
                ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)
            firestoreDataSource.deleteUserProfile(uid)
            authRepository.deleteUser()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nell'eliminazione dell'utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    private fun mapExceptionToDataError(e: Exception): DataError {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.NOT_FOUND -> DataError.User.USER_NOT_FOUND
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> DataError.User.USER_ALREADY_EXISTS
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.User.PERMISSION_DENIED
                else -> DataError.Network.SERVER_ERROR
            }

            is StorageException -> when (e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND -> DataError.Storage.BUCKET_NOT_FOUND
                StorageException.ERROR_NOT_AUTHENTICATED -> DataError.Auth.NOT_AUTHENTICATED
                StorageException.ERROR_QUOTA_EXCEEDED -> DataError.Storage.QUOTA_EXCEEDED
                else -> DataError.Storage.UPLOAD_FAILED
            }

            is FirebaseException -> DataError.Network.UNKNOWN
            else -> DataError.Network.UNKNOWN
        }
    }
}