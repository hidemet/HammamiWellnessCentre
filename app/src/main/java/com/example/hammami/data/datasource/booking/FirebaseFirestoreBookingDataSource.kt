package com.example.hammami.data.datasource.booking

import com.example.hammami.domain.model.Booking
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

    suspend fun saveBooking(booking: Booking) : DocumentReference {
        try {
            return bookingsCollection.add(booking).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    fun updateBooking(transaction: Transaction, bookingId: String, status: BookingStatus)) {
        try {
            val bookingRef = bookingsCollection.document(bookingId)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun getBookingsForDate(date: Date): List<Booking> {
        return try {
            bookingsCollection
                .whereEqualTo("date", date)
                .get()
                .await()
                .toObjects(Booking::class.java)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            bookingsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects(Booking::class.java)
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