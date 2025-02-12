package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.user.FirebaseFirestoreUserDataSource
import com.example.hammami.data.datasource.user.FirebaseStorageUserDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val storageDataSource: FirebaseStorageUserDataSource,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {

    private val _isAdmin = MutableStateFlow<Boolean?>(null)
    val isAdmin: StateFlow<Boolean?> = _isAdmin.asStateFlow()

    suspend fun isUserAdmin(userId: String): Result<Boolean, DataError> {
        return try {
            val isAdmin = firestoreDataSource.checkIfAdmin(userId)
            _isAdmin.value = isAdmin
            Result.Success(isAdmin)
        } catch (e: Exception) {
            Log.e("UserRepository", "isUserAdmin: Error for user ID: $userId", e)
            _isAdmin.value = false
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserById(userId: String): Result<User, DataError> {
        return try {
            val user = firestoreDataSource.fetchUserData(userId)
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel recupero dei dati utente per ID: $userId", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun deductPoints(
        transaction: Transaction,
        userId: String,
        pointsToDeduct: Int
    ): Result<Unit, DataError> {
        try {
            val userDocument = firestore.collection("users").document(userId)
            val userSnapshot = transaction.get(userDocument)
            val currentPoints = userSnapshot.getLong("points")?.toInt() ?: 0

            if (currentPoints < pointsToDeduct) {
                return Result.Error(DataError.User.INSUFFICIENT_POINTS)
            }

            transaction.update(userDocument, "points", currentPoints - pointsToDeduct)
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(mapExceptionToDataError(e))
        }
    }


    fun addUserPoints(
        transaction: Transaction,
        userId: String,
        pointsToAdd: Int
    ): Result<Unit, DataError> {
        return try {
            firestoreDataSource.addUserPoints(transaction, userId, pointsToAdd)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user points for user: $userId", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserData(): Result<User, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val user = firestoreDataSource.fetchUserData(uidResult.data)
                    if (user != null) {
                        Result.Success(user)
                    } else {
                        Result.Error(DataError.User.USER_NOT_FOUND)
                    }
                } catch (e: Exception) {
                    Result.Error(mapExceptionToDataError(e))
                }
            }

            is Result.Error -> Result.Error(uidResult.error)
        }
    }

     suspend fun getCurrentUserFirstName(): Result<String, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val firstName = firestoreDataSource.fetchUserFirstName(uidResult.data)
                    Result.Success(firstName)
                } catch (e: Exception) {
                    Result.Error(mapExceptionToDataError(e))
                }
            }
            is Result.Error -> Result.Error(uidResult.error)
        }
    }

    suspend fun deductPoints(userId: String, requiredPoints: Int): Result<Unit, DataError> {
        return try {
            val userPoints = firestoreDataSource.getUserPoints(userId)
            firestoreDataSource.setUserPoints(userId, userPoints - requiredPoints)
            Result.Success(Unit)
        } catch (e: Exception) {
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
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun signUp(email: String, password: String, userData: User): Result<User, DataError> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
        }

        return when (val authResult = authRepository.createUser(email, password)) {
            is Result.Success -> {
                when (val uidResult = authRepository.getCurrentUserId()) {
                    is Result.Success -> {
                        when (val saveResult = saveUser(uidResult.data, userData)) {
                            is Result.Success -> Result.Success(userData.copy(email = email))
                            is Result.Error -> {
                                // Rollback: elimina l'account appena creato se il salvataggio del profilo fallisce
                                authRepository.deleteUser()
                                Result.Error(saveResult.error)
                            }
                        }
                    }

                    is Result.Error -> {
                        authRepository.deleteUser()
                        Result.Error(uidResult.error)
                    }
                }
            }

            is Result.Error -> Result.Error(authResult.error)
        }
    }

    suspend fun updateUser(user: User): Result<Unit, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    firestoreDataSource.updateUser(uidResult.data, user)
                    Result.Success(Unit)

                } catch (e: Exception) {
                    Result.Error(mapExceptionToDataError(e))
                }
            }

            is Result.Error -> Result.Error(uidResult.error)
        }
    }

    private suspend fun saveUser(userUid: String, user: User): Result<Unit, DataError> {
        return try {
            firestoreDataSource.saveUserInformation(userUid, user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun uploadUserImage(imageUri: Uri): Result<String, DataError> {
        return try {
            val downloadUrl = storageDataSource.uploadUserImage(imageUri)
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun deleteUser(): Result<Unit, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    firestoreDataSource.deleteUserProfile(uidResult.data)
                    when (val deleteResult = authRepository.deleteUser()) {
                        is Result.Success -> Result.Success(Unit)
                        is Result.Error -> deleteResult
                    }
                } catch (e: Exception) {
                    Result.Error(mapExceptionToDataError(e))
                }
            }

            is Result.Error -> Result.Error(uidResult.error)
        }
    }

    suspend fun getUserPoints(): Result<Int, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val points = firestoreDataSource.getUserPoints(uidResult.data)
                    Result.Success(points)
                } catch (e: Exception) {
                    Result.Error(mapExceptionToDataError(e))
                }
            }

            is Result.Error -> Result.Error(uidResult.error)
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