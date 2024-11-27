package com.example.hammami.domain.usecase.coupon

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.user.getCurrentUserIdUseCase
import javax.inject.Inject

class CreateCouponUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val getCurrentUserIdUseCase: getCurrentUserIdUseCase
) {
    suspend operator fun invoke(
        value: Double,
    ): Result<DiscountVoucher, DataError> {
        return when (val uidResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                val coupon = DiscountVoucher(
                    code = DiscountVoucher.generateCode(value, VoucherType.COUPON),
                    value = value,
                    type = VoucherType.COUPON,
                    expirationDate = DiscountVoucher.calculateExpirationDate(),
                    createdBy = uidResult.data,
                )

                voucherRepository.createVoucher(coupon)
            }

            is Result.Error -> Result.Error(uidResult.error)
        }
    }
}