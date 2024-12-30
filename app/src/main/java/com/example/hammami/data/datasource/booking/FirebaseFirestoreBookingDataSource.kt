package com.example.hammami.data.datasource.booking

import android.util.Log
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingDto
import com.example.hammami.domain.model.BookingStatus
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreBookingDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val bookingsCollection = firestore.collection("Bookings")

//    suspend fun reserveTimeSlot(
//        serviceId: String,
//        date: Date,
//        timeSlot: String,
//        userId: String
//    ) {
//        try {
//            bookingsCollection.add(
//                Booking(
//                    serviceId = serviceId,
//                    date = date,
//                    time = timeSlot,
//                    userId = userId
//                )
//            ).await()
//        } catch (e: FirebaseFirestoreException) {
//            throw e
//        }
//    }

    suspend fun saveBooking(booking: Booking): DocumentReference {
        try {
            return bookingsCollection.add(booking).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    fun updateBooking(transaction: Transaction, bookingId: String, status: BookingStatus) {
        try {
            val bookingRef = bookingsCollection.document(bookingId)
            transaction.update(bookingRef, "status", status)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun getBookingsForDate(date: Date): List<Booking> {
        try {
            val querySnapshot = bookingsCollection
                .whereEqualTo("date", date)
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

//    suspend fun createBooking(booking: Booking) {
//        try {
//            bookingsCollection.document().set(booking).await()
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//
//    suspend fun getAvailableOperators(
//        date: String,
//        startTime: String,
//        endTime: String
//    ): List<Int> {
//        try {
//            // Recupera le prenotazioni esistenti che si sovrappongono con l'intervallo richiesto
//            val existingBookings = bookingsCollection
//                .whereEqualTo("date", date)
//                .whereGreaterThan("endTime", startTime)
//                .whereLessThan("startTime", endTime)
//                .get()
//                .await()
//                .toObjects(Booking::class.java)
//
//            // Trova gli operatori già occupati in questo intervallo
//            val busyOperators = existingBookings.map { it.operator }.toSet()
//
//            // Restituisce gli operatori disponibili
//            return (1..3).filter { !busyOperators.contains(it) }
//        } catch (e: Exception) {
//            throw e
//        }
//    }
}