package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError

import javax.inject.Inject

class GenerateCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(value: Double): Result<Coupon, DataError> {
        return when {
            value <= 0 -> Result.Error(DataError.User.INVALID_INPUT)
            else -> couponRepository.generateCoupon(value)
        }
    }
}
