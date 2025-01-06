package com.example.hammami.data.datasource.booking

import android.util.Log
import com.example.hammami.domain.model.Booking
import com.example.hammami.data.entity.BookingDto
import com.example.hammami.domain.model.BookingStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreBookingDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val bookingsCollection = firestore.collection("Bookings")


    fun generateBookingId(): String {
        return bookingsCollection.document().id
    }


    suspend fun saveBooking(booking: Booking) {
        try {
            bookingsCollection.document(booking.id).set(booking).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    fun updateBooking(transaction: Transaction, bookingId: String, status: BookingStatus, amount: Double) {
        try {
            val bookingRef = bookingsCollection.document(bookingId)
            transaction.update(bookingRef,"price", amount)
            transaction.update(bookingRef, "status", status)
        } catch (e: FirebaseFirestoreException) {
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

    suspend fun getBookingsForDate(dateMillis: Long): List<Booking> {
        try {
            val querySnapshot = bookingsCollection
                .whereEqualTo("date", dateMillis)
                .get()
                .await()
            return querySnapshot.documents.mapNotNull {
                it.toObject(BookingDto::class.java)?.toBooking()
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreBookingDataSource", "Errore nel recuperare le prenotazioni per data", e)
            throw e
        }
    }

    suspend fun getUserBookings(userId: String): List<Booking> {
        try {
            val querySnapshot = bookingsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            return querySnapshot.documents.mapNotNull {
                it.toObject(BookingDto::class.java)?.toBooking()
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreBookingDataSource", "Errore nel recuperare le prenotazioni dell'utente", e)
            throw e
        }
    }

    suspend fun getBookingById(bookingId: String): Booking {
        return try {
            val documentSnapshot = bookingsCollection.document(bookingId).get().await()
            val bookingDto = documentSnapshot.toObject(BookingDto::class.java)
                ?: throw FirebaseFirestoreException(
                    "Booking not found",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            return bookingDto.toBooking()
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