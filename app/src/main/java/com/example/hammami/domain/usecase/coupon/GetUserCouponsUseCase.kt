package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.user.getCurrentUserIdUseCase
import javax.inject.Inject

class GetUserCouponsUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val getCurrentUserIdUseCase: getCurrentUserIdUseCase
) {
    suspend operator fun invoke(): Result<List<DiscountVoucher>, DataError> {
        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                try {
                    // Recupera solo i voucher di tipo COUPON dell'utente
                    voucherRepository.getUserVouchers(
                        userId = userResult.data,
                        type = VoucherType.COUPON
                    )
                } catch (e: Exception) {
                    Result.Error(DataError.Network.UNKNOWN)
                }
            }
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}