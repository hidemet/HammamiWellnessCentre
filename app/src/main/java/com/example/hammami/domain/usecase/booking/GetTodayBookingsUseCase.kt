package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.time.DateTimeUtils.toEndOfDayTimestamp
import com.example.hammami.core.time.DateTimeUtils.toStartOfDayTimestamp
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
     operator fun invoke(): Flow<Result<List<Booking>, DataError>> {
        val today = LocalDate.now()
        val startOfDay = today.toStartOfDayTimestamp()
        val endOfDay = today.toEndOfDayTimestamp()

        return bookingRepository.getBookingsForDateRange(startOfDay, endOfDay)
    }
}