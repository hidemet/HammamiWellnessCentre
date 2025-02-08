package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.time.TimeSlotCalculator
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class IsTimeSlotAvailableUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(
        date: LocalDate,
        slot: TimeSlot
    ): Result<Boolean, DataError> {
        // Converti LocalTime in Timestamp per la query al database
        val startTimestamp = DateTimeUtils.toTimestamp(date, slot)
        val endTimestamp = DateTimeUtils.toTimestamp(date, slot.endTime)

        return bookingRepository.isTimeSlotAvailable(startTimestamp, endTimestamp)
    }
}