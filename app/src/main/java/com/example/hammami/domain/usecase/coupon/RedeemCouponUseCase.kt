package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.usecase.user.DeductUserPointsUseCase
import com.example.hammami.domain.usecase.user.getCurrentUserIdUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError

import javax.inject.Inject

class RedeemCouponUseCase @Inject constructor(
    private val createCouponUseCase: CreateCouponUseCase,
    private val deductPointsUseCase: DeductUserPointsUseCase,
    private val getCurrentUserIdUseCase: getCurrentUserIdUseCase,
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(value: Double, requiredPoints: Int): Result<DiscountVoucher, DataError> {
        return when (val userIdResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                val userId = userIdResult.data

                // 1. Crea il coupon
                when (val couponResult = createCouponUseCase( userId, value)) {
                    is Result.Success -> {
                        // 2. Sottrae i punti
                        when (val deductResult = deductPointsUseCase(userId, requiredPoints)) {
                            is Result.Success -> Result.Success(couponResult.data)
                            is Result.Error -> {
                                // Rollback: elimina il coupon creato
                                voucherRepository.deleteVoucher(couponResult.data.code)
                                Result.Error(deductResult.error)
                            }
                        }
                    }
                    is Result.Error -> couponResult
                }
            }
            is Result.Error -> Result.Error(userIdResult.error)
        }
    }
}