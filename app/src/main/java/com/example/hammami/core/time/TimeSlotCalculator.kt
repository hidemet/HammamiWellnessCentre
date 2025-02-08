package com.example.hammami.core.time

import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.booking.IsTimeSlotAvailableUseCase
import java.time.LocalDate
import javax.inject.Inject

class TimeSlotCalculator @Inject constructor(
    private val timeSlotGenerator: TimeSlotGenerator, // Dipendenza da TimeSlotGenerator
    private val isTimeSlotAvailableUseCase: IsTimeSlotAvailableUseCase
) {

    suspend fun generateAvailableTimeSlots(
        serviceDurationMinutes: Int,
        date: LocalDate
    ): List<TimeSlot> {
        // Usa TimeSlotGenerator per generare la lista POTENZIALE di slot
        val potentialSlots = timeSlotGenerator.generateTimeSlots(serviceDurationMinutes)
        val availableSlots = mutableListOf<TimeSlot>()

        potentialSlots.forEach { slot ->


            // Verifica la disponibilità di ogni singolo slot usando IsTimeSlotAvailableUseCase
            val isBookedResult = isTimeSlotAvailableUseCase(
                date = date,
                slot = slot
            )
            when (isBookedResult) {
                is Result.Success -> {
                    if (!isBookedResult.data) {
                        availableSlots.add(slot)
                    }
                }

                is Result.Error -> Unit
            }
        }
        return availableSlots.sortedBy { it.startTime }
    }
}
