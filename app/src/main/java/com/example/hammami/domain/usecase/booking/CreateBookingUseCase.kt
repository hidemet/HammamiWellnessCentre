package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.repositories.BookingRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import java.util.Date


class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(
        service: Service,
        selectedDate: Date,
        startTime: String,
        endTime: String,
        operatorId: Int,
        status: BookingStatus = BookingStatus.RESERVED
    ): Result<Booking, DataError> {
        return bookingRepository.createBooking(service, selectedDate, startTime, endTime, operatorId, status)
    }
}