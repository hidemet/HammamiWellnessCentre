package com.example.hammami.data.datasource.booking

import com.example.hammami.domain.model.Booking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreBookingDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val bookingsCollection = firestore.collection("bookings")

    suspend fun createBooking(booking: Booking) {
        try {
            bookingsCollection.document().set(booking).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAvailableOperators(
        date: String,
        startTime: String,
        endTime: String
    ): List<Int> {
        try {
            // Recupera le prenotazioni esistenti che si sovrappongono con l'intervallo richiesto
            val existingBookings = bookingsCollection
                .whereEqualTo("date", date)
                .whereGreaterThan("endTime", startTime)
                .whereLessThan("startTime", endTime)
                .get()
                .await()
                .toObjects(Booking::class.java)

            // Trova gli operatori già occupati in questo intervallo
            val busyOperators = existingBookings.map { it.operator }.toSet()

            // Restituisce gli operatori disponibili
            return (1..3).filter { !busyOperators.contains(it) }
        } catch (e: Exception) {
            throw e
        }
    }
}