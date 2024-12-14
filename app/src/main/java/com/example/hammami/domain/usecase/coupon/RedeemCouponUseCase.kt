package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError

import javax.inject.Inject

class RedeemCouponUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(value: Double, requiredPoints: Int): Result<Voucher, DataError> {
        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                voucherRepository.redeemVoucher(userResult.data, requiredPoints, value, Voucher.Type.COUPON)
            }
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}