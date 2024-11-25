package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class ApplyCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(couponCode: String, bookingId: String, amount: Double): Result<Unit, DataError> {
        if (couponCode.isBlank() ) {
            return Result.Error(DataError.User.INVALID_INPUT)
        }
        if (bookingId.isBlank()) {
            return Result.Error(DataError.User.INVALID_INPUT)
        }

        // Validazione del coupon prima dell'utilizzo
        return when (val validationResult = couponRepository.getValidatedCoupon(couponCode, amount)) {
            is Result.Success -> couponRepository.useCoupon(couponCode, bookingId)
            is Result.Error -> Result.Error(validationResult.error)
        }
    }
}