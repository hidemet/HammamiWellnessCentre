package com.example.hammami.domain.usecase.booking
//
//import com.example.hammami.data.repositories.BookingRepository
//import com.example.hammami.domain.error.DataError
//import com.example.hammami.core.result.Result
//import com.example.hammami.domain.model.Booking
//import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
//import java.util.Date
//import javax.inject.Inject
//
//class ReserveTimeSlotUseCase @Inject constructor(
//    private val bookingRepository: BookingRepository,
//    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
//) {
//    suspend operator fun invoke(
//        serviceId: String,
//        date: Date,
//        timeSlot: String,
//    ): Result<Unit, DataError> {
//        return when (val userResult = getCurrentUserIdUseCase()) {
//            is Result.Success -> bookingRepository.reserveTimeSlot(
//                serviceId,
//                date,
//                timeSlot,
//                userResult.data
//            )
//
//            is Result.Error -> Result.Error(userResult.error)
//        }
//    }
//}