package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class GetActiveCouponsUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(): Result<List<Coupon>, DataError> {
        return when (val result = couponRepository.getUserCoupons()) {
            is Result.Success -> Result.Success(result.data.filter { it.isValid() })
            is Result.Error -> Result.Error(result.error)
        }
    }
}