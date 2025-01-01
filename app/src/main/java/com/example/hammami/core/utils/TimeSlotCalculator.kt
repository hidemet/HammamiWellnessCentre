package com.example.hammami.core.utils

import kotlinx.datetime.format
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


private const val OPEN_HOUR = 10
private const val CLOSE_HOUR = 19


class TimeSlotCalculator @Inject constructor() {

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun generateAvailableTimeSlots(
        date: Date,
        serviceDurationMinutes: Int,
        bookedAppointments: List<BookedTimeSlot>,
    ): List<AvailableSlot> {
        val availableSlots = mutableListOf<AvailableSlot>()
        val startTime = LocalTime.of(OPEN_HOUR, 0)
        val endTime = LocalTime.of(CLOSE_HOUR, 0)
        val serviceDuration = Duration.ofMinutes(serviceDurationMinutes.toLong())

        var currentSlotStart = startTime
        while (currentSlotStart.isBefore(endTime)) {
            val currentSlotEnd = currentSlotStart.plus(serviceDuration)

            // Verifica se lo slot termina dopo l'orario di chiusura
            if (!currentSlotEnd.isAfter(endTime)) {
                val formattedSlotStart = currentSlotStart.format(timeFormatter)
                val formattedSlotEnd = currentSlotEnd.format(timeFormatter)

                // Verifica se lo slot è già prenotato
                val isBooked = bookedAppointments.any { bookedSlot ->
                    isTimeSlotOverlapping(
                        formattedSlotStart,
                        formattedSlotEnd,
                        bookedSlot.startTime,
                        bookedSlot.endTime
                    )
                }

                if (!isBooked) {
                    availableSlots.add(AvailableSlot(formattedSlotStart, formattedSlotEnd))
                }
            }
            currentSlotStart = currentSlotEnd // Il prossimo slot inizia alla fine di quello attuale
        }

        return availableSlots.sortedBy { it.startTime }
    }

     fun isTimeSlotOverlapping(
        slotStart: String,
        slotEnd: String,
        bookedStart: String,
        bookedEnd: String
    ): Boolean {
        val slotStartTime = LocalTime.parse(slotStart, timeFormatter)
        val slotEndTime = LocalTime.parse(slotEnd, timeFormatter)
        val bookedStartTime = LocalTime.parse(bookedStart, timeFormatter)
        val bookedEndTime = LocalTime.parse(bookedEnd, timeFormatter)

        return bookedStartTime.isBefore(slotEndTime) && slotStartTime.isBefore(bookedEndTime)
    }

    data class BookedTimeSlot(val startTime: String, val endTime: String)
    data class AvailableSlot(val startTime: String, val endTime: String)

}