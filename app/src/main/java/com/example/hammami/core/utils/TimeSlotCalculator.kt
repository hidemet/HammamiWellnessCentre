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
private const val SLOT_INTERVAL_MINUTES = 30

class TimeSlotCalculator @Inject constructor() {

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun generateAvailableTimeSlots(
        date: Date,
        serviceDurationMinutes: Int,
        bookedAppointments: List<BookedTimeSlot>,
        numberOfOperators: Int
    ): List<AvailableSlot> {
        val availableSlots = mutableListOf<AvailableSlot>()
        val startTime = LocalTime.of(OPEN_HOUR, 0)
        val endTime = LocalTime.of(CLOSE_HOUR, 0)
        val serviceDuration = Duration.ofMinutes(serviceDurationMinutes.toLong())
        // val slotInterval = Duration.ofMinutes(SLOT_INTERVAL_MINUTES.toLong())

        var currentSlotStart = startTime
        while (currentSlotStart.isBefore(endTime)) {
            val currentSlotEnd = currentSlotStart.plus(serviceDuration)

            // Verifica se lo slot termina dopo l'orario di chiusura
            if (!currentSlotEnd.isAfter(endTime)) {
                val formattedSlotStart = currentSlotStart.format(timeFormatter)
                val formattedSlotEnd = currentSlotEnd.format(timeFormatter)

                // Creo una mappa per tenere traccia degli operatori disponibili per questo slot
                val availableOperatorsForSlot = mutableMapOf<Int, Boolean>()
                for (operatorId in 1..numberOfOperators) {
                    availableOperatorsForSlot[operatorId] = true
                }

                // Controllo le disponibilità degli operatori in base alle prenotazioni esistenti
                for (bookedSlot in bookedAppointments) {
                    if (isTimeSlotOverlapping(
                            formattedSlotStart,
                            formattedSlotEnd,
                            bookedSlot.startTime,
                            bookedSlot.endTime
                        )
                    ) {
                        // Se lo slot è occupato, segno l'operatore come non disponibile
                        availableOperatorsForSlot[bookedSlot.operatorId] = false
                    }
                }
                // Aggiungo lo slot (con operatori disponibili) alla lista
                for (operatorId in 1..numberOfOperators) {
                    if (availableOperatorsForSlot[operatorId] == true) {
                        availableSlots.add(
                            AvailableSlot(
                                formattedSlotStart,
                                formattedSlotEnd,
                                operatorId
                            )
                        )
                    }
                }
            }
            currentSlotStart = if (currentSlotStart.minute == 0) {
                currentSlotStart.plusMinutes(30)
            } else {
                currentSlotStart.plusHours(1).withMinute(0)
            }
        }

        return availableSlots.distinct().sortedBy { it.startTime }
    }

    private fun isTimeSlotOverlapping(
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

    data class BookedTimeSlot(val startTime: String, val endTime: String, val operatorId: Int)
    data class AvailableSlot(val startTime: String, val endTime: String, val operatorId: Int)

}