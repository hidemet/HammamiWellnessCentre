
package com.example.hammami.core.time

import android.util.Log
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

private const val OPEN_HOUR = 10
private const val CLOSE_HOUR = 19

class TimeSlotGenerator @Inject constructor() {

    fun generateTimeSlots(serviceDurationMinutes: Int): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        val startTime = LocalTime.of(OPEN_HOUR, 0)
        val endTime = LocalTime.of(CLOSE_HOUR, 0)
        val serviceDuration = Duration.ofMinutes(serviceDurationMinutes.toLong())

        var currentSlotStart = startTime
        while (currentSlotStart.isBefore(endTime)) {
            val currentSlotEnd = currentSlotStart.plus(serviceDuration)

            if (!currentSlotEnd.isAfter(endTime)) {
                timeSlots.add(TimeSlot(currentSlotStart, currentSlotEnd))
            }
            currentSlotStart = currentSlotEnd
        }
        Log.d("TimeSlotGenerator", "Generated slots: $timeSlots")
        return timeSlots.sortedBy { it.startTime }
    }
}