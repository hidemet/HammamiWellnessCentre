package com.example.hammami.domain.usecase

import com.example.hammami.domain.error.ValidationError.DiscountError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.DiscountVoucher
import javax.inject.Inject

class ValidateVoucherUseCase @Inject constructor() {
    operator fun invoke(
        voucher: DiscountVoucher,
        amount: Double
    ): Result<Unit, DiscountError> {
        return when {
            !voucher.canBeAppliedTo(amount) ->
                Result.Error(DiscountError.EXCEEDS_AMOUNT)
            else -> Result.Success(Unit)
        }
    }
}