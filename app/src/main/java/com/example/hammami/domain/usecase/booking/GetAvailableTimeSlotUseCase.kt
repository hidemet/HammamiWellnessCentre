package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.core.utils.TimeSlotCalculator
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import java.util.Date
import javax.inject.Inject

private const val NUMBER_OF_OPERATORS = 3
class GetAvailableTimeSlotsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val timeSlotCalculator: TimeSlotCalculator
) {
    suspend operator fun invoke(
        serviceId: String,
        date: Date,
        serviceDurationMinutes: Int
    ): Result<List<String>, DataError> {
        return when (val existingBookingsResult = bookingRepository.getBookingsForDate(date)) {
            is Result.Success -> {
                val bookedTimeSlots = existingBookingsResult.data.map {
                    TimeSlotCalculator.BookedTimeSlot(
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                }
                val availableSlots = timeSlotCalculator.generateAvailableTimeSlots(
                    date = date,
                    serviceDurationMinutes = serviceDurationMinutes,
                    bookedAppointments = bookedTimeSlots,
                    numberOfOperators = NUMBER_OF_OPERATORS
                )
                Result.Success(availableSlots)
            }
            is Result.Error -> Result.Error(existingBookingsResult.error)
        }
    }
}