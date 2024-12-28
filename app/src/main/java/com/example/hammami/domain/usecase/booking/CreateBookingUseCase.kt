package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.repositories.BookingRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(
        service: Service,
        selectedDate: String,
        selectedTimeSlot: String,
        status: BookingStatus = BookingStatus.RESERVED
    ): Result<Booking, DataError> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val selectedLocalTime = LocalTime.parse(selectedTimeSlot, timeFormatter)

        val serviceDuration = Duration.ofMinutes(service.length?.toLong() ?: 60)
        val endTime = selectedLocalTime.plus(serviceDuration)



        val bookingEndTime = endTime.format(timeFormatter)
        val bookingStartTime = selectedLocalTime.format(timeFormatter)

        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                val booking = Booking(
                    serviceId = service.id,
                    serviceName = service.name,
                    date = selectedDate,
                    startTime = bookingStartTime,
                    endTime = bookingEndTime,
                    status = status,
                    userId = userResult.data,
                )
              bookingRepository.saveBooking(booking)
                Result.Success(booking)
            }
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}