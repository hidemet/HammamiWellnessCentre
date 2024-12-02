package com.example.hammami.domain.usecase.voucher

import com.example.hammami.domain.error.ValidationError.VoucherError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.Voucher
import javax.inject.Inject

class ValidateVoucherUseCase @Inject constructor() {
    operator fun invoke(
        voucher: Voucher,
        amount: Double
    ): Result<Unit, VoucherError> {
        return when {
            voucher.value > amount ->
                Result.Error(VoucherError.EXCEEDS_AMOUNT)
            voucher.isExpired ->
                Result.Error(VoucherError.EXPIRED)
            else -> Result.Success(Unit)
        }
    }
}