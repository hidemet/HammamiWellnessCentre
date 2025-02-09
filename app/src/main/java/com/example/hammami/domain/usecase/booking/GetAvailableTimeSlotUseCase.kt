package com.example.hammami.domain.usecase.booking

import android.util.Log
import com.example.hammami.core.result.Result
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.core.time.TimeSlotCalculator
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
    ): Result<List<TimeSlot>, DataError> {
Log.d("GetAvailableTimeSlotsUseCase", "Date: $date") //LOG
        return try {
            val availableSlots = timeSlotCalculator.generateAvailableTimeSlots(
                serviceDurationMinutes = serviceDurationMinutes,
                date = date
            )
            Log.d("GetAvailableTimeSlotsUseCase", "Available Slots: $availableSlots") //LOG
         val filteredSlots = filterSlotsForToday(availableSlots, date)
            Result.Success(filteredSlots)
        } catch (e: Exception) {
            Result.Error(DataError.Booking.SLOT_NOT_AVAILABLE)
        }
    }

    private fun filterSlotsForToday(slots: List<TimeSlot>, date: LocalDate): List<TimeSlot> {
        val today = LocalDate.now()
        return if (date.isEqual(today)) {
            val now = LocalTime.now()
            slots.filter { slot -> slot.startTime.isAfter(now) }
        } else {
            slots
        }
    }
}