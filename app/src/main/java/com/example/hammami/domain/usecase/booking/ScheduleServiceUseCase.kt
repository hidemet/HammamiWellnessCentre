package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import javax.inject.Inject

class ScheduleServiceUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(serviceAppointment: Booking): Result<Unit, DataError> {
        return bookingRepository.saveBooking(serviceAppointment)
    }
}