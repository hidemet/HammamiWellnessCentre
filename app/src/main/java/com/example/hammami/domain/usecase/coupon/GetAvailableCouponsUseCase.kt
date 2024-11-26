package com.example.hammami.domain.usecase.coupon

import com.example.hammami.domain.model.coupon.AvailableVoucher
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.usecase.user.GetUserPointsUseCase
import javax.inject.Inject

class GetAvailableCouponsUseCase @Inject constructor(
    private val getUserPointsUseCase: GetUserPointsUseCase
) {
    suspend operator fun invoke(): Result<List<AvailableVoucher>, DataError> {
        return when (val pointsResult = getUserPointsUseCase()) {
            is Result.Success -> Result.Success(
                AvailableVoucher.createCouponOptions(pointsResult.data)
            )

            is Result.Error -> Result.Error(pointsResult.error)
        }
    }
}