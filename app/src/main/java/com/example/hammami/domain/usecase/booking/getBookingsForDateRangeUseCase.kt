package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.mapper.BookingMapper
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.core.result.Result
import com.google.firebase.Timestamp

import javax.inject.Inject

class GetBookingsForDateRangeUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val bookingMapper: BookingMapper
) {
    //Modificato per usare Timestamp
    suspend operator fun invoke(startDate: Timestamp, endDate: Timestamp): Result<List<Booking>, DataError> {

        return bookingRepository.getBookingsForDateRange(startDate, endDate)
    }
}