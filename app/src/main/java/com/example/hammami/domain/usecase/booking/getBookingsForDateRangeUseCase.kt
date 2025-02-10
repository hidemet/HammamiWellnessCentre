package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.mapper.BookingMapper
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.core.result.Result
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class GetBookingsForDateRangeUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
     operator fun invoke(startDate: Timestamp, endDate: Timestamp): Flow<Result<List<Booking>, DataError>> {
        return bookingRepository.getBookingsForDateRange(startDate, endDate)
    }
}