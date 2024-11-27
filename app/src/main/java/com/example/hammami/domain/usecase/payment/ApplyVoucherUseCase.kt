package com.example.hammami.domain.usecase.payment


import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.ValidationError.*
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.payment.Discount
import javax.inject.Inject


class ApplyVoucherUseCase @Inject constructor() {
    data class DiscountedAmount(
        val finalAmount: Double,
        val savings: Double
    )

    operator fun invoke(
        amount: Double,
        discount: DiscountVoucher
    ): Result<DiscountedAmount, DiscountError> {
        return when {
            !discount.isValid() ->
                Result.Error(DiscountError.INVALID_DISCOUNT)

            !discount.canBeAppliedTo(amount) ->
                Result.Error(DiscountError.EXCEEDS_AMOUNT)

            else -> Result.Success(
                DiscountedAmount(
                    finalAmount = amount - discount.value,
                    savings = discount.value
                )
            )
        }
    }
}