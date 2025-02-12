package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.BookingRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserBookingsFlowUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    operator fun invoke(
    ): Flow<Result<List<Booking>, DataError>> {
        return bookingRepository.getBookingsForUser()
    }
}