package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.core.utils.TimeSlotCalculator
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class GetAvailableTimeSlotsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val timeSlotCalculator: TimeSlotCalculator
) {
    suspend operator fun invoke(
        date: LocalDate,
        serviceDurationMinutes: Int
    ): Result<List<TimeSlotCalculator.AvailableSlot>, DataError> {
        return when (val existingBookingsResult = bookingRepository.getBookingsForDate(date)) {
            is Result.Success -> {
                val bookedTimeSlots = existingBookingsResult.data.map {
                    TimeSlotCalculator.BookedTimeSlot(
                        startTime = it.startTime,
                        endTime = it.endTime,
                    )
                }
                val availableSlots = timeSlotCalculator.generateAvailableTimeSlots(
                    serviceDurationMinutes = serviceDurationMinutes,
                    bookedAppointments = bookedTimeSlots,
                )

               val filteredSlots = filterSlotsForToday(availableSlots, date)

                Result.Success(filteredSlots)
            }

            is Result.Error -> Result.Error(existingBookingsResult.error)
        }
    }

    private fun filterSlotsForToday(slots: List<TimeSlotCalculator.AvailableSlot>, date: LocalDate): List<TimeSlotCalculator.AvailableSlot> {
        val today = LocalDate.now()
        val isToday = date.isEqual(today)
        val now = LocalTime.now()

        return if (isToday) {
            slots.filter { slot ->
                LocalTime.parse(slot.startTime).isAfter(now)
            }
        } else {
            slots
        }
    }
}