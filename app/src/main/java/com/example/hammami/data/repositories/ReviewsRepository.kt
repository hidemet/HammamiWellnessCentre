package com.example.hammami.data.repositories

import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import android.util.Log
import com.example.hammami.data.datasource.booking.FirebaseFirestoreBookingDataSource
import com.example.hammami.domain.model.Review
import com.example.hammami.data.datasource.reviews.FirebaseFirestoreReviewsDataSource
import com.example.hammami.data.datasource.services.FirebaseFirestoreMainCategoryDataSource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Error
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewsRepository @Inject constructor(
    private val reviewsDataSource: FirebaseFirestoreReviewsDataSource,
    private val serviceDataSource: FirebaseFirestoreMainCategoryDataSource,
    private val bookingDataSource: FirebaseFirestoreBookingDataSource,
    private val firestore: FirebaseFirestore
) {


    suspend fun createReview(
        reviewText: String,
        rating: Float,
        serviceName: String,
        userName: String,
        bookingId: String):Result<Review,DataError> {

        val review = Review(
            commento = reviewText,
            utente = userName,
            valutazione = rating
        )

        val serviceId = serviceDataSource.getIdServiceFromName(serviceName) ?: return Result.Error(DataError.Service.SERVICE_NOT_FOUND)
        val servicePath = serviceDataSource.getPathFromServiceId(serviceId)

        Log.d("ReviewsRepository", "ID del servizio: $serviceId")
        return try {
            firestore.runTransaction { transaction ->
                // 1. Crea la recensione
                Log.d("ReviewsRepository", "Creazione della recensione")
                val reviewId = reviewsDataSource.addReviewData(transaction,review)
                Log.d("ReviewsRepository", "Recensione creata con ID: $reviewId")

                // 2. Aggiungi la recensione al servizio
                Log.d("ReviewsRepository", "Aggiunta della recensione al servizio")
                serviceDataSource.addReviewToAService(transaction,servicePath, reviewId)
                Log.d("ReviewsRepository", "Recensione aggiunta al servizio con ID: $serviceId")

                // 3. Aggiorna lo stato della prenotazione
                Log.d("ReviewsRepository", "Aggiornamento dello stato della prenotazione")
                bookingDataSource.setBookingHasReview(transaction, bookingId)
                Log.d("ReviewsRepository", "Stato della prenotazione aggiornato con ID: $bookingId")

            }.await()

            Log.d("ReviewsRepository", "Recensione creata con successo")
            Result.Success(review)

        } catch (e: Exception) {
            Log.d("ReviewsRepository", "Errore nella creazione della recensione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getReviewsData(reviewsPath: List<DocumentReference>?): Result<List<Review>, DataError> {
        return try {
            val reviewsService = reviewsDataSource.fetchReviewsData(reviewsPath)
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


    suspend fun getCollectionFromService(serviceId: String): Result<String, DataError> {
        return try {
            val collection = serviceDataSource.getCollectionPathFromServiceId(serviceId)
            Result.Success(collection)
        } catch (e: Exception) {
            Log.e("ReviewsRepository", "Errore nel recupero della collezione di recensioni associate al servizio selezionato", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getServiceIdFromName(name: String): Result<String?, DataError> {
        return try {
            val serviceId = serviceDataSource.getIdServiceFromName(name)
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