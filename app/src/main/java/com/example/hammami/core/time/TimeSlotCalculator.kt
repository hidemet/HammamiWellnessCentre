package com.example.hammami.core.time

import android.util.Log
import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.booking.IsTimeSlotAvailableUseCase
import java.time.LocalDate
import javax.inject.Inject

class TimeSlotCalculator @Inject constructor(
    private val timeSlotGenerator: TimeSlotGenerator,
    private val isTimeSlotAvailableUseCase: IsTimeSlotAvailableUseCase
) {

    suspend fun generateAvailableTimeSlots(
        serviceDurationMinutes: Int,
        date: LocalDate
    ): List<TimeSlot> {
        val potentialSlots = timeSlotGenerator.generateTimeSlots(serviceDurationMinutes, date)

        if (potentialSlots.isEmpty()) {
            return emptyList()
        }

        val availableSlots = mutableListOf<TimeSlot>()

        for (slot in potentialSlots) {
          //  val startTimestamp = DateTimeUtils.toTimestamp(date, slot.startTime)
         //   val endTimestamp = DateTimeUtils.toTimestamp(date, slot.endTime)



            when (val isAvailableResult = isTimeSlotAvailableUseCase(date = date, slot = slot)) {
                is Result.Success -> {
                    if (isAvailableResult.data) {
                        availableSlots.add(slot)
                    } else {
                    }
                }
                is Result.Error -> {
                    Log.e("TimeSlotCalculator", "Error checking availability for slot $slot: ${isAvailableResult.error}") // LOG

                }
            }
        }

        return availableSlots.sortedBy { it.startTime }
    }
}