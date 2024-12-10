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
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val storageDataSource: FirebaseStorageUserDataSource,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) {
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

    suspend fun getUserPoints(userId: String): Result<Int, DataError> {
        return try {
            val points = firestoreDataSource.getUserPoints(userId)
            Result.Success(points)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
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


//    suspend fun deductPointsAndAddVoucher(
//        userId: String,
//        requiredPoints: Int,
//        voucher: Voucher
//    ): Result<Unit, DataError> {
//        return try {
//            val userPoints = firestoreDataSource.getUserPoints(userId)
//            firestoreDataSource.setUserPoints(userId, userPoints - requiredPoints)
//            firestoreDataSource.addVoucher(userId, voucher)
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Result.Error(mapExceptionToDataError(e))
//        }
//    }


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