package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.datasource.booking.FirebaseFirestoreBookingDataSource
import com.example.hammami.data.mapper.BookingMapper
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val bookingDataSource: FirebaseFirestoreBookingDataSource,
    private val authRepository: AuthRepository,
    private val bookingMapper: BookingMapper
) {

    suspend fun createBooking(
        service: Service,
        startDate: Timestamp,
        endDate: Timestamp,
        status: BookingStatus,
        price: Double
    ): Result<Booking, DataError> {
        return try {
            authRepository.getCurrentUserId().let { userIdResult ->
                when (userIdResult) {
                    is Result.Success -> {
                        val userId = userIdResult.data
                        val bookingId = bookingDataSource.generateBookingId()

                        val booking = Booking(
                            id = bookingId,
                            serviceId = service.id,
                            serviceName = service.name,
                            startDate = startDate,
                            endDate = endDate,
                            status = status,
                            userId = userId,
                            price = price
                        )
                        bookingDataSource.saveBooking(booking.let { bookingMapper.toDto(it) })
                        Result.Success(booking)
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
        status: BookingStatus,
        amount: Double,
        transactionId: String
    ): Result<Unit, DataError> {
        return try {
            if (bookingId.isBlank()) {
                return Result.Error(DataError.Booking.BOOKING_NOT_FOUND)
            }
            bookingDataSource.updateBooking(transaction, bookingId, status, amount, transactionId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nell'aggiornare la prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun updateBookingDetails(
        bookingId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<Unit, DataError> {
        return try {
            if (bookingId.isBlank()) {
                return Result.Error(DataError.Booking.BOOKING_NOT_FOUND)
            }
            bookingDataSource.updateBookingDetails(bookingId, startDate, endDate)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nell'aggiornare i dettagli della prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun updateBookingReview(bookingId: String): Result<Unit, DataError> {
        return try {
            if (bookingId.isBlank()) {
                return Result.Error(DataError.Booking.BOOKING_NOT_FOUND)
            }
            bookingDataSource.updateBookingReview(bookingId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nell'aggiornare la recensione della prenotazione", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun getBookingsForDateRange(
        startDate: Timestamp,
        endDate: Timestamp
    ): Flow<Result<List<Booking>, DataError>> {
        return try {
            bookingDataSource.getBookingsForDateRange(startDate, endDate)
                .map { dtoList ->
                    Result.Success(dtoList.map { bookingMapper.toDomain(it) })
                }
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel recuperare le prenotazioni per l'intervallo di date", e)
            flowOf(Result.Error(mapExceptionToDataError(e)))
        }

    }

    suspend fun isTimeSlotAvailable(
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<Boolean, DataError> {
        return try {
            val isAvailable = bookingDataSource.isTimeSlotAvailable(startDate, endDate)
            Log.d("BookingRepository", "isTimeSlotAvailable: startDate=$startDate, endDate=$endDate, isAvailable=$isAvailable")
            Result.Success(isAvailable)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel verificare la disponibilità dello slot", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserBookings(userId: String): Result<List<Booking>, DataError> {
        return try {
            val bookings =
                bookingDataSource.getUserBookings(userId).map { bookingMapper.toDomain(it) }
            Result.Success(bookings)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel recuperare le prenotazioni dell'utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserBookingsSeparated(userId: String): Result<Pair<List<Booking>, List<Booking>>, DataError> {
        return try {
            Log.d("BookingRepository", "getUserBookingsSeparated: Fetching bookings for user: $userId") // LOG
            val bookings =
                bookingDataSource.getUserBookings(userId).map { bookingMapper.toDomain(it) }
            val currentDate = Timestamp.now()

            val pastBookings = bookings.filter { it.startDate < currentDate }
            val futureBookings = bookings.filter { it.startDate >= currentDate }
            Log.d("BookingRepository", "getUserBookingsSeparated: Past bookings: ${pastBookings.size}, Future bookings: ${futureBookings.size}") // LOG
            Result.Success(Pair(pastBookings, futureBookings))
        } catch (e: Exception) {
            Log.e("BookingRepository", "Errore nel recuperare le prenotazioni dell'utente", e)
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getBookingById(bookingId: String): Result<Booking, DataError> {
        return try {
            val bookingDto = bookingDataSource.getBookingById(bookingId)
            if (bookingDto != null) {
                Result.Success(bookingMapper.toDomain(bookingDto))
            } else {
                Result.Error(DataError.Booking.BOOKING_NOT_FOUND)
            }
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