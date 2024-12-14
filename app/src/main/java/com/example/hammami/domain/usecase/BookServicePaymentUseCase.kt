package com.example.hammami.domain.usecase

//class BookServicePaymentUseCase @Inject constructor(
//    private val processPaymentUseCase: ProcessPaymentUseCase,
//    private val bookingRepository: BookingRepository
//) {
//    suspend operator fun invoke(
//        paymentSystem: PaymentSystem,
//        amount: Double,
//        serviceId: String
//    ): Result<BookingResult, DataError> {
//        return when (val paymentResult = processPaymentUseCase(amount, paymentSystem)) {
//            is Result.Success -> {
//                val transactionId = paymentResult.data
//                when (val bookingResult = bookingRepository.createBooking(
//                    serviceId = serviceId,
//                    transactionId = transactionId
//                )) {
//                    is Result.Success -> Result.Success(
//                        BookingResult(
//                            transactionId = transactionId,
//                            booking = bookingResult.data
//                        )
//                    )
//                    is Result.Error -> Result.Error(bookingResult.error)
//                }
//            }
//            is Result.Error -> Result.Error(paymentResult.error)
//        }
//    }
//
//    data class BookingResult(
//        val transactionId: String,
//        val booking: Booking
//    )
//}