package com.example.hammami.data.repositories

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import android.util.Log
import com.example.hammami.domain.model.Service
import com.example.hammami.data.datasource.services.FirebaseFirestoreEsteticaDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EsteticaRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreEsteticaDataSource
) {
    suspend fun getEpilazioneData(): Result<List<Service>, DataError> {
        return try {
            val serviceEpilazione = firestoreDataSource.fetchEpilazioneData()
            if (serviceEpilazione.isNotEmpty()) {
                Result.Success(serviceEpilazione)
            } else {
                Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("EsteticaRepository", "Errore nel recupero del catalogo dei trattamenti di epilazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getTrattCorpoData(): Result<List<Service>, DataError> {
        return try {
            val serviceTrattCorpo = firestoreDataSource.fetchTrattCorpoData()
            if (serviceTrattCorpo.isNotEmpty()) {
                Result.Success(serviceTrattCorpo)
            } else {
                Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("EsteticaRepository", "Errore nel recupero del catalogo dei trattamenti del corpo", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getTrattVisoData(): Result<List<Service>, DataError> {
        return try {
            val serviceTrattViso = firestoreDataSource.fetchTrattVisoData()
            if (serviceTrattViso.isNotEmpty()) {
                Result.Success(serviceTrattViso)
            } else {
                Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("EsteticaRepository", "Errore nel recupero del catalogo dei trattamenti del viso", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    /*
    suspend fun getEpilazioneData(serviceId: String?): Result<Service, DataError> {
        return try {
            if (serviceId == null) {
                return Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
            val serviceData = firestoreDataSource.fetchEpilazioneData(serviceId)
            if (serviceId != null) {
                Result.Success(Data)
            } else {
                Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Errore nel recupero dei dati utente per ID: $userId", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getTrattVisoData(serviceId: String?): Result<Service, DataError> {
        return try {
            if (serviceId == null) {
                return Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
            val serviceTrattViso = firestoreDataSource.fetchTrattVisoData(serviceId)
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

    suspend fun getTrattCorpoData(serviceId: String?): Result<Service, DataError> {
        return try {
            if (serviceId == null) {
                return Result.Error(DataError.Service.SERVICE_NOT_FOUND)
            }
            val serviceTrattCorpo = firestoreDataSource.fetchTrattCorpoData(serviceId)
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