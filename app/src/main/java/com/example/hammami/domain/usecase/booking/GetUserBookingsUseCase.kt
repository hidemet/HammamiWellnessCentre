package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import javax.inject.Inject

class GetUserBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Booking>, DataError> {
        return bookingRepository.getUserBookings(userId)
    }
}