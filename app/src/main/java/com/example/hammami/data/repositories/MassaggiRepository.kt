package com.example.hammami.data.repositories

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import android.util.Log
import com.example.hammami.domain.model.Service
import com.example.hammami.data.datasource.services.FirebaseFirestoreMassaggiDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MassaggiRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreMassaggiDataSource,
) {
    suspend fun getMassaggiData(): Result<List<Service>, DataError> {
        return try {
            val serviceMassaggi = firestoreDataSource.fetchMassaggiData()
            if (serviceMassaggi.isNotEmpty()) {
                Result.Success(serviceMassaggi)
            } else {
                Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("MassaggiRepository", "Errore nel recupero del catalogo dei massaggi", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    /*
    suspend fun getMassagguData(serviceId: String?): Result<Service, DataError> {
        return try {
            if (serviceId == null) {
                return Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
            val serviceMassaggi = firestoreDataSource.fetchMassaggiData(serviceId)
            if (userData != null) {
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