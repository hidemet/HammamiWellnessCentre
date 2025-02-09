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
        Log.d("TimeSlotCalculator", "generateAvailableTimeSlots called with date: $date, duration: $serviceDurationMinutes") // LOG IMPORTANTE

        val potentialSlots = timeSlotGenerator.generateTimeSlots(serviceDurationMinutes)
        Log.d("TimeSlotCalculator", "Potential slots generated: $potentialSlots") // LOG

        val availableSlots = mutableListOf<TimeSlot>()

        for (slot in potentialSlots) {
            val startTimestamp = DateTimeUtils.toTimestamp(date, slot.startTime)
            val endTimestamp = DateTimeUtils.toTimestamp(date, slot.endTime)

            Log.d("TimeSlotCalculator", "Checking slot: $slot, startTimestamp: $startTimestamp, endTimestamp: $endTimestamp") // LOG


            when (val isAvailableResult = isTimeSlotAvailableUseCase(date = date, slot = slot)) {
                is Result.Success -> {
                    Log.d("TimeSlotCalculator", "isTimeSlotAvailableUseCase returned: ${isAvailableResult.data}") // LOG
                    if (isAvailableResult.data) {
                        availableSlots.add(slot)
                    } else {
                        Log.d("TimeSlotCalculator", "Slot $slot è NON disponibile") // LOG
                    }
                }
                is Result.Error -> {
                    Log.e("TimeSlotCalculator", "Error checking availability for slot $slot: ${isAvailableResult.error}") // LOG

                }
            }
        }

        Log.d("TimeSlotCalculator", "Final available slots: $availableSlots") // LOG
        return availableSlots.sortedBy { it.startTime }
    }
}