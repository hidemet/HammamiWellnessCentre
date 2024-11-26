package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class CreateVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        value: Double,
        type: VoucherType,
        userId: String,
        transactionId: String,
    ) : Result<DiscountVoucher, DataError> {
        return when(type) {
            VoucherType.COUPON -> voucherRepository.createCoupon(value, userId, calculateRequiredPoints(value))
            VoucherType.GIFT_CARD -> voucherRepository.createGiftCard(value, userId, transactionId)
        }
    }

    private fun calculateRequiredPoints(value: Double): Int {
        return (value * 5).toInt()
    }
}

