package com.example.hammami.domain.usecase.validation.discount


import com.example.hammami.domain.model.payment.Discount
import com.example.hammami.domain.error.ValidationError
import com.example.hammami.core.result.Result

import javax.inject.Inject

class ValidateVoucherUseCase @Inject constructor() {
    operator fun invoke(
        discount: Discount,
        amount: Double
    ): Result<Unit, ValidationError.DiscountError> {
        return when {
            !discount.isValid() ->
                Result.Error(ValidationError.DiscountError.INVALID_DISCOUNT)
            !discount.canBeAppliedTo(amount) ->
                Result.Error(ValidationError.DiscountError.EXCEEDS_AMOUNT)
            else -> Result.Success(Unit)
        }
    }
}
