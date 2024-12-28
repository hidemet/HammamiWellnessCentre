package com.example.hammami.domain.usecase.booking

import com.example.hammami.data.repositories.BookingRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import java.util.Calendar
import java.util.Date


class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(
        service: Service,
        selectedDate: Date,
        selectedTimeSlot: String,
        status: BookingStatus = BookingStatus.RESERVED
    ): Result<Booking, DataError> {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        val hour = selectedTimeSlot.split(":")[0].toInt()
        val minute = selectedTimeSlot.split(":")[1].toInt()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val endTimeCalendar = calendar.clone() as Calendar
        endTimeCalendar.add(Calendar.MINUTE, service.length?.toInt() ?: 60)

        val bookingEndTime = String.format(
            "%02d:%02d",
            endTimeCalendar.get(Calendar.HOUR_OF_DAY),
            endTimeCalendar.get(Calendar.MINUTE)
        )

        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                val booking = Booking(
                    serviceId = service.id,
                    serviceName = service.name,
                    date = calendar.time,
                    startTime = selectedTimeSlot,
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