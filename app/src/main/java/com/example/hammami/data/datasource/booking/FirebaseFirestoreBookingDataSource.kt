package com.example.hammami.data.datasource.booking

import android.util.Log
import com.example.hammami.data.entity.BookingDto
import com.example.hammami.domain.model.BookingStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreBookingDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val bookingsCollection = firestore.collection("Bookings")


    fun generateBookingId(): String {
        return bookingsCollection.document().id
    }

    fun setBookingHasReview(transaction: Transaction, bookingId: String) {
        val bookingRef = bookingsCollection.document(bookingId)
        transaction.update(bookingRef, "hasReview", true)
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



     fun getBookingsForDateRange(
        startDate: Timestamp,
        endDate: Timestamp
    ): Flow<List<BookingDto>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        try {
            val querySnapshot = bookingsCollection
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("startDate", endDate)
                .orderBy("startDate")

            listenerRegistration = querySnapshot.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val bookings = snapshot.documents.mapNotNull { it.toObject(BookingDto::class.java) }
                    trySend(bookings)
                } else {
                    trySend(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreDataSource", "Error fetching bookings for date range", e)
            throw e
        }

        awaitClose { // Deregistra il listener quando il flow non è più necessario
            Log.d("FirestoreDataSource", "Cleaning up Firestore listener (getBookingsForDateRange)")
            listenerRegistration.remove()
        }

    }

    fun getBookingsForUser(
        userId: String,
    ): Flow<List<BookingDto>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            val query = bookingsCollection
                .whereEqualTo("userId", userId)
                .orderBy("startDate")

            listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDataSource", "getBookingsForUserAndDateRange: Error: ${error.message}", error) // LOG errore
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val bookings = snapshot.documents.mapNotNull { it.toObject(BookingDto::class.java) }
                    trySend(bookings)
                } else {
                    Log.d("FirestoreDataSource", "getBookingsForUserAndDateRange: Snapshot is null")  // LOG
                    trySend(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreDataSource", "getBookingsForUserAndDateRange: Exception: ${e.message}", e) // LOG eccezione
            close(e)
        }

        awaitClose {
            Log.d("FirestoreDataSource", "Cleaning up Firestore listener (getBookingsForUserAndDateRange)")
            listenerRegistration?.remove()
        }
    }

    suspend fun isTimeSlotAvailable(startDate: Timestamp, endDate: Timestamp): Boolean {
        return try {
            val querySnapshot = bookingsCollection
                .whereLessThan("startDate", endDate)
                .whereGreaterThan("endDate", startDate)
                .get()
                .await()

            val isAvailable = querySnapshot.isEmpty // Se la query è vuota, lo slot è disponibile
            isAvailable

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
            bookingRef.update("hasReview", true)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun getUserBookings(userId: String): List<BookingDto> {
        try {
            Log.d("FirestoreBookingDataSource", "getUserBookings: Querying for userId: $userId") // LOG
            val querySnapshot = bookingsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val bookings = querySnapshot.documents.mapNotNull {
                it.toObject(BookingDto::class.java)
            }
            Log.d("FirestoreBookingDataSource", "getUserBookings: Found ${bookings.size} bookings") // LOG

            return bookings

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