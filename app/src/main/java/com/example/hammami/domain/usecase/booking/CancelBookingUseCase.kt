package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): Result<Unit, DataError> {
        return bookingRepository.deleteBooking(bookingId)
    }
}