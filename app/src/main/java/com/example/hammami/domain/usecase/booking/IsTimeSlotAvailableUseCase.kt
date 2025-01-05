package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.time.TimeSlotCalculator
import java.time.LocalDate
import javax.inject.Inject

class IsTimeSlotAvailableUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val timeSlotCalculator: TimeSlotCalculator // Inietta TimeSlotCalculator
) {
    suspend operator fun invoke(
        date: LocalDate,
        slot: TimeSlotCalculator.AvailableSlot
    ): Result<Boolean, DataError> {
        return when (val existingBookingsResult = bookingRepository.getBookingsForDate(date)) {
            is Result.Success -> {
                val isAvailable = !existingBookingsResult.data.any { booking ->
                    timeSlotCalculator.isTimeSlotOverlapping(
                        slot.startTime,
                        slot.endTime,
                        booking.startTime,
                        booking.endTime
                    )
                }
                Result.Success(isAvailable)
            }
            is Result.Error -> Result.Error(existingBookingsResult.error)
        }
    }
}