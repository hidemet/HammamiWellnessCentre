package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.datasource.booking.FirebaseFirestoreBookingDataSource
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val bookingDataSource: FirebaseFirestoreBookingDataSource,
    private val authRepository: AuthRepository
) {

//    suspend fun reserveTimeSlot(
//        serviceId: String,
//        date: Date,
//        timeSlot: String,
//        userId: String
//    ): Result<Unit, DataError> {
//        return try {
//           bookingDataSource.reserveTimeSlot(serviceId, date, timeSlot, userId)
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Log.e("BookingRepository", "Errore nel prenotare lo slot", e)
//            Result.Error(mapExceptionToDataError(e))
//        }
//    }
//    )

    suspend fun saveBooking(booking: Booking): Result<Unit, DataError> {
        return try {
           val documentReference = bookingDataSource.saveBooking(booking)
            booking.id = documentReference.id
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel salvare la prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getBookingsForDate(date: Date): Result<List<Booking>, DataError> {
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

//    companion object {
//        private const val MIN_OPERATOR = 1
//        private const val MAX_OPERATOR = 3
//        private const val OPENING_HOUR = 10
//        private const val CLOSING_HOUR = 19
//    }
//
//// prova
//    // testo
//    // testo
//    suspend fun createBooking(serviceId: String, date: String, startTime: String, serviceDuration: Int): Result<Unit, DataError> {
//        return try {
//            // Recupera l'ID dell'utente corrente
//            when (val userIdResult = authRepository.getCurrentUserId()) {
//                is Result.Success -> {
//                    val endTime = LocalTime.parse(startTime).plusMinutes(serviceDuration.toLong()).toString()
//
//                    // Verifica se lo slot è ancora disponibile
//                    val isSlotAvailable = checkSlotAvailability(date, startTime, endTime)
//                    if (!isSlotAvailable) {
//                        return Result.Error(DataError.Booking.SLOT_NO_LONGER_AVAILABLE)
//                    }
//
//                    // Trova un operatore disponibile
//                    val availableOperators = bookingDataSource.getAvailableOperators(date, startTime, endTime)
//                    if (availableOperators.isEmpty()) {
//                        return Result.Error(DataError.Booking.NO_OPERATORS_AVAILABLE)
//                    }
//
//                    val booking = Booking(
//                        userId = userIdResult.data,
//                        serviceId = serviceId,
//                        date = date,
//                        startTime = startTime,
//                        endTime = endTime,
//                        operator = availableOperators.first(),
//                        status = BookingStatus.CONFIRMED
//                    )
//
//                    bookingDataSource.createBooking(booking)
//                    Result.Success(Unit)
//                }
//                is Result.Error -> Result.Error(userIdResult.error)
//            }
//        } catch (e: Exception) {
//            Result.Error(mapExceptionToDataError(e))
//        }
//    }
//
//    suspend fun getAvailableSlots(date: String, serviceDuration: Int): Result<List<BookingSlot>, DataError> {
//        return try {
//            // Verifica se la data è valida (non passata e giorno di apertura)
//            if (!isValidBookingDate(date)) {
//                return Result.Error(DataError.Booking.INVALID_DATE)
//            }
//
//            val slots = mutableListOf<BookingSlot>()
//            var currentTime = LocalTime.of(OPENING_HOUR, 0)
//            val closingTime = LocalTime.of(CLOSING_HOUR, 0)
//
//            // Se è oggi, inizia dall'ora successiva
//            if (date == LocalDate.now().toString()) {
//                val now = LocalTime.now()
//                currentTime = now.withMinute(0).plusHours(1)
//                if (currentTime.isBefore(LocalTime.of(OPENING_HOUR, 0))) {
//                    currentTime = LocalTime.of(OPENING_HOUR, 0)
//                }
//            }
//
//            while (currentTime.plusMinutes(serviceDuration.toLong()) <= closingTime) {
//                val endTime = currentTime.plusMinutes(serviceDuration.toLong())
//
//                val availableOperators = bookingDataSource.getAvailableOperators(
//                    date,
//                    currentTime.toString(),
//                    endTime.toString()
//                )
//
//                if (availableOperators.isNotEmpty()) {
//                    slots.add(
//                        BookingSlot(
//                            startTime = currentTime,
//                            endTime = endTime
//                        )
//                    )
//                }
//
//                // Passa allo slot successivo
//                currentTime = currentTime.plusHours(1)
//            }
//
//            Result.Success(slots)
//        } catch (e: Exception) {
//            Result.Error(mapExceptionToDataError(e))
//        }
//    }
//
//    private fun isValidBookingDate(date: String): Boolean {
//        val bookingDate = LocalDate.parse(date)
//        val today = LocalDate.now()
//
//        if (bookingDate.isBefore(today)) {
//            return false
//        }
//
//        // Verifica se è un giorno di apertura (martedì-sabato)
//        val dayOfWeek = bookingDate.dayOfWeek
//        return dayOfWeek !in listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
//    }
//
//    private fun mapExceptionToDataError(e: Exception): DataError = when (e) {
//        is FirebaseFirestoreException -> DataError.Network.SERVER_ERROR
//        is FirebaseException -> DataError.Network.NO_INTERNET
//        else -> DataError.Unknown.UNKNOWN
//    }
}