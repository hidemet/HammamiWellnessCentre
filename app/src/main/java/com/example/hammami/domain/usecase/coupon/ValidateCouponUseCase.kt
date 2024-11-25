package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.util.CouponConstants
import javax.inject.Inject

class ValidateCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(code: String, amount: Double): Result<Coupon, DataError> {
        if (!isValidCouponInput(code)) {
            return Result.Error(DataError.User.INVALID_INPUT)
        }
        return couponRepository.getValidatedCoupon(code,amount)
    }

    private fun isValidCouponInput(code: String): Boolean =
        code.isNotBlank() && CouponConstants.COUPON_CODE_PATTERN.matches(code)
}