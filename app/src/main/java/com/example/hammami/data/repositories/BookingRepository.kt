package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.datasource.booking.FirebaseFirestoreBookingDataSource
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val bookingDataSource: FirebaseFirestoreBookingDataSource,
    private val authRepository: AuthRepository
) {

    suspend fun createBooking(
        service: Service,
        selectedDate: LocalDate,
        startTime: String,
        endTime: String,
        status: BookingStatus
    ): Result<Booking, DataError> {
        return try {
            authRepository.getCurrentUserId().let { userIdResult ->
                when (userIdResult) {
                    is Result.Success -> {
                        val userId = userIdResult.data
                        val booking = Booking(
                            serviceId = service.id,
                            serviceName = service.name,
                            date = selectedDate.toEpochDay()*(24*60*60*1000),
                            startTime = startTime,
                            endTime = endTime,
                            status = status,
                            userId = userId,
                        )
                        val bookingDocument = bookingDataSource.saveBooking(booking)
                        Log.d("BookingRepository", "Prenotazione creata con successo bookingDocument.id: ${bookingDocument.id}")
                        Result.Success(booking.copy(id = bookingDocument.id))
                    }

                    is Result.Error -> {
                        Result.Error(DataError.Auth.NOT_AUTHENTICATED)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel creare la prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun updateBooking(
        transaction: Transaction,
        bookingId: String,
        status: BookingStatus
    ): Result<Unit, DataError> {
        return try {
            if(bookingId.isBlank()) {
                return Result.Error(DataError.Booking.BOOKING_NOT_FOUND)
            }
            bookingDataSource.updateBooking(transaction, bookingId, status)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nell'aggiornare la prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getBookingsForDate(date: LocalDate): Result<List<Booking>, DataError> {
        return try {
            val bookings = bookingDataSource.getBookingsForDate(date)
            Result.Success(bookings)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel recuperare le prenotazioni per data", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserBookings(userId: String): Result<List<Booking>, DataError> {
        return try {
            val bookings = bookingDataSource.getUserBookings(userId)
            Result.Success(bookings)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel recuperare le prenotazioni dell'utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getBookingById(bookingId: String): Result<Booking, DataError> {
        return try {
            val booking = bookingDataSource.getBookingById(bookingId)
            Result.Success(booking)
        } catch (e: Exception) {
        Log.e("BookingRepository", "Errore nel recuperare la prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun deleteBooking(bookingId: String): Result<Unit, DataError> {
        return try {
            bookingDataSource.deleteBooking(bookingId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nell'eliminare la prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    private fun mapExceptionToDataError(e: Exception): DataError {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.NOT_FOUND -> DataError.Booking.BOOKING_NOT_FOUND
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> DataError.Booking.BOOKING_ALREADY_EXISTS
                else -> DataError.Network.SERVER_ERROR
            }

            is FirebaseException -> DataError.Network.UNKNOWN
            else -> DataError.Network.UNKNOWN
        }
    }
}