package com.example.hammami.data.repositories

import com.google.firebase.FirebaseException
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.example.hammami.domain.model.Service
import com.example.hammami.data.datasource.services.FirebaseFirestoreMainCategoryDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BestDealsRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreMainCategoryDataSource
) {

    suspend fun getBestDealsData(): Result<List<Service>, DataError> {
        return try {
            val serviceBestDeals = firestoreDataSource.fetchBestDeals()
            if (serviceBestDeals.isNotEmpty()) {
                Result.Success(serviceBestDeals)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    /*
    suspend fun getBenessereData(benessereId: String?): Result<Service, DataError> {
        return try {
            if (benessereId == null) {
                return Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
            val servizioBenessere = firestoreDataSource.fetchBenessereData(benessereId)
            if (servizioBenessere != null) {
                Result.Success(userData)
            } else {
                Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel recupero dei dati utente per ID: $userId", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }
     */

    private fun mapExceptionToDataError(e: Exception): DataError {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.NOT_FOUND -> DataError.Service.SERVICE_NOT_FOUND
                else -> DataError.Network.SERVER_ERROR
            }

            is StorageException -> when (e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND -> DataError.Storage.BUCKET_NOT_FOUND
                StorageException.ERROR_QUOTA_EXCEEDED -> DataError.Storage.QUOTA_EXCEEDED
                else -> DataError.Storage.UPLOAD_FAILED
            }

            is FirebaseException -> DataError.Network.UNKNOWN
            else -> DataError.Network.UNKNOWN
        }
    }
}