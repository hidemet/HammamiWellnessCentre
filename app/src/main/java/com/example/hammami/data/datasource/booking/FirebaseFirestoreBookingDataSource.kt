package com.example.hammami.data.datasource.booking

import android.util.Log
import com.example.hammami.domain.model.Booking
import com.example.hammami.data.entity.BookingDto
import com.example.hammami.data.mapper.BookingMapper
import com.example.hammami.domain.model.BookingStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreBookingDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val bookingMapper: BookingMapper
) {
    private val bookingsCollection = firestore.collection("Bookings")


    fun generateBookingId(): String {
        return bookingsCollection.document().id
    }


    suspend fun saveBooking(bookingDto: BookingDto) {
        try {
            bookingsCollection.document(bookingDto.id!!).set(bookingDto).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    fun updateBooking(
        transaction: Transaction,
        bookingId: String,
        status: BookingStatus,
        amount: Double,
        transactionId: String
    ) {
        try {
            val bookingRef = bookingsCollection.document(bookingId)
            transaction.update(bookingRef, "price", amount)
            transaction.update(bookingRef, "status", status)
            transaction.update(bookingRef, "transactionId", transactionId)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun updateBookingDetails(
        bookingId: String,
        startDate: Timestamp,
        endDate: Timestamp,
    ) {
        try {
            val bookingRef = bookingsCollection.document(bookingId)
            val updates = hashMapOf<String, Any>(
                "startDate" to startDate,
                "endDate" to endDate
            )
            bookingRef.update(updates).await()

        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun getBookingsForDateRange(
        startDate: Timestamp,
        endDate: Timestamp
    ): List<BookingDto> {
        try {
            val querySnapshot = bookingsCollection
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("startDate", endDate)
                .get()
                .await()

            return querySnapshot.documents.mapNotNull {
                it.toObject(BookingDto::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirestoreDataSource", "Error fetching bookings for date range", e)
            throw e
        }
    }

    suspend fun isTimeSlotAvailable(startDate: Timestamp, endDate: Timestamp): Boolean {
        return try {
            val querySnapshot = bookingsCollection
                .whereLessThanOrEqualTo("startDate", endDate)
                .whereGreaterThanOrEqualTo("endDate", startDate)
                .limit(1)
                .get()
                .await()

            querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e(
                "FirestoreBookingDataSource",
                "Errore nel verificare la disponibilità dello slot",
                e
            )
            throw e
        }
    }

    fun updateBookingReview(bookingId: String) {
        try {
            val bookingRef = bookingsCollection.document(bookingId)
            Log.e("FirestoreBookingDataSource", "$bookingRef")
            bookingRef.update("hasReview", true)
            Log.e("FirestoreBookingDataSource", "Impostato hasReview a true")
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun getUserBookings(userId: String): List<BookingDto> {
        try {
            val querySnapshot = bookingsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            return querySnapshot.documents.mapNotNull {
                it.toObject(BookingDto::class.java)
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(
                "FirestoreBookingDataSource",
                "Errore nel recuperare le prenotazioni dell'utente",
                e
            )
            throw e
        }
    }

    suspend fun getBookingById(bookingId: String): BookingDto? {
        try {
            val documentSnapshot = bookingsCollection.document(bookingId).get().await()
            return documentSnapshot.toObject(BookingDto::class.java)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun deleteBooking(bookingId: String) {
        try {
            bookingsCollection.document(bookingId).delete().await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
}