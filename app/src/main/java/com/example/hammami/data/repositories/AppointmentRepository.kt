package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.core.result.Result
import com.example.hammami.data.datasource.appointment.FirebaseAppointmentDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.ServiceAppointment
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val firestoreDataSource: FirebaseAppointmentDataSource
) {

    suspend fun getNewAppointmentData(clientEmail: String): Result<List<ServiceAppointment>, DataError> {
        return try {
            val appointment = firestoreDataSource.fetchNewAppointmentData(clientEmail)
            if (appointment.isNotEmpty()) {
                Result.Success(appointment)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("AppointmentRepository", "Errore nel recupero dei dati degli appuntamenti futuri di $clientEmail", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getPastAppointmentData(clientEmail: String): Result<List<ServiceAppointment>, DataError> {
        return try {
            val appointment = firestoreDataSource.fetchPastAppointmentData(clientEmail)
            if (appointment.isNotEmpty()) {
                Result.Success(appointment)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)
            }
        } catch (e: Exception) {
            Log.e("AppointmentRepository", "Errore nel recupero dei dati degli appuntamenti passati di $clientEmail", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

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