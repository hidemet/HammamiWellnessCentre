package com.example.hammami.domain.usecase.booking

import android.util.Log
import com.example.hammami.core.result.Result
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import java.time.LocalDate
import javax.inject.Inject

class IsTimeSlotAvailableUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(
        date: LocalDate,
        slot: TimeSlot
    ): Result<Boolean, DataError> {
        val startTimestamp = DateTimeUtils.toTimestamp(date, slot)
        val endTimestamp = DateTimeUtils.toTimestamp(date, slot.endTime)
        Log.d("IsTimeSlotAvailableUseCase", "Checking availability: start=$startTimestamp, end=$endTimestamp") // LOG
        return bookingRepository.isTimeSlotAvailable(startTimestamp, endTimestamp)
    }
}