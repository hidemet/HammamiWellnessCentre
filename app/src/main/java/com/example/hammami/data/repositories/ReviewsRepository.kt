package com.example.hammami.data.repositories

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import android.util.Log
import com.example.hammami.domain.model.Review
import com.example.hammami.data.datasource.reviews.FirebaseFirestoreReviewsDataSource
import com.example.hammami.data.datasource.services.FirebaseFirestoreMainCategoryDataSource
import com.google.firebase.firestore.DocumentReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewsRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreReviewsDataSource,
    private val firestoreDataSourceService: FirebaseFirestoreMainCategoryDataSource
) {

    suspend fun getReviewsData(reviewsPath: List<DocumentReference>?): Result<List<Review>, DataError> {
        return try {
            val reviewsService = firestoreDataSource.fetchReviewsData(reviewsPath)
            if (reviewsService.isNotEmpty()) {
                Result.Success(reviewsService)
            } else {
                Result.Error(DataError.User.USER_NOT_FOUND)    //DA PERSONALIZZARE
            }
        } catch (e: Exception) {
            Log.e("ReviewsRepository", "Errore nel recupero dei dati delle recensione associate al servizio selezionato", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun addReviewData(review: Review): Result<Pair<String, List<Review>>, DataError> {
        return try {
            val documentId = firestoreDataSource.addReviewData(review)
            Result.Success(Pair(documentId, listOf(review)))
        } catch (e: Exception) {
            Log.e("ReviewsRepository", "Errore nell'aggiunta della recensione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun addReviewToService(serviceId: String, reviewId: String): Result<Unit, DataError> {
        return try {
            val documentId = firestoreDataSourceService.addReviewToAService(serviceId, reviewId)
            Result.Success(documentId)
        } catch (e: Exception) {
            Log.e("ReviewsRepository", "Errore nell'aggiunta della recensione", e)
            Result.Error(mapExceptionToDataError(e))
        }

    }

    suspend fun getCollectionFromService(serviceId: String): Result<String, DataError> {
        return try {
            val collection = firestoreDataSourceService.getCollectionPathFromServiceId(serviceId)
            Result.Success(collection)
        } catch (e: Exception) {
            Log.e("ReviewsRepository", "Errore nel recupero della collezione di recensioni associate al servizio selezionato", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getServiceIdFromName(name: String): Result<String?, DataError> {
        return try {
            val serviceId = firestoreDataSourceService.getIdServiceFromName(name)
            Result.Success(serviceId)
        } catch (e: Exception) {
            Log.e("ReviewsRepository", "Errore nel recupero dell'id del servizio", e)
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