package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.repositories.BookingRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date


class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(
        service: Service,
        startDate: Timestamp,
        endDate: Timestamp,
        status: BookingStatus = BookingStatus.RESERVED,
        price: Double
    ): Result<Booking, DataError> {
        return bookingRepository.createBooking(service, startDate,endDate, status,price)
    }
}