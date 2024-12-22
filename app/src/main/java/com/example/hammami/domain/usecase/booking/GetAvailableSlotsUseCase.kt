package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.presentation.ui.features.booking.BookingSlot
import javax.inject.Inject

class GetAvailableSlotsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(date: String, serviceDuration: Int): Result<List<BookingSlot>, DataError> {
        return bookingRepository.getAvailableSlots(date, serviceDuration)
    }
}
