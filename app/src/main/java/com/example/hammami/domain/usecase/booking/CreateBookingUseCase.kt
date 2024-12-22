package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.repositories.BookingRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError


class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        serviceId: String,
        date: String,
        startTime: String,
        serviceDuration: Int
    ): Result<Unit, DataError> {
        return bookingRepository.createBooking(
            serviceId = serviceId,
            date = date,
            startTime = startTime,
            serviceDuration = serviceDuration
        )
    }
}