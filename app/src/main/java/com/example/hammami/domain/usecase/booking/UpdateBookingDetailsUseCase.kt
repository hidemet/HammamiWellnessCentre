package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.repositories.BookingRepository
import javax.inject.Inject
import com.example.hammami.domain.error.DataError
import com.google.firebase.Timestamp
import com.example.hammami.core.result.Result


class UpdateBookingDetailsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<Unit, DataError> {
        return bookingRepository.updateBookingDetails(bookingId, startDate, endDate)
    }
}