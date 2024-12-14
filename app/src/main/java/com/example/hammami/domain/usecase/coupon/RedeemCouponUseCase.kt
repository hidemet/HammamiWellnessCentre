package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.usecase.user.DeductUserPointsUseCase
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.voucher.CreateVoucherUseCase

import javax.inject.Inject

class RedeemCouponUseCase @Inject constructor(
    private val createVoucherUseCase: CreateVoucherUseCase,
    private val deductPointsUseCase: DeductUserPointsUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(value: Double, requiredPoints: Int): Result<Voucher, DataError> {
        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                // 1. Crea il coupon
                when (val couponResult = createVoucherUseCase(value, VoucherType.COUPON)) {
                    is Result.Success -> {
                        // 2. Sottrae i punti
                        when (val deductResult = deductPointsUseCase(userResult.data, requiredPoints)) {
                            is Result.Success -> Result.Success(couponResult.data)
                            is Result.Error -> {
                                // Rollback: elimina il coupon creato
                                couponResult.data.code.let { code ->
                                    voucherRepository.deleteVoucher(code)
                                }
                                Result.Error(deductResult.error)
                            }
                        }
                    }
                    is Result.Error -> couponResult
                }
            }
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}