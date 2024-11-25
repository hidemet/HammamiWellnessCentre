package com.example.hammami.domain.usecase.validation.discount

import com.example.hammami.data.repositories.DiscountRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.payment.Discount
import javax.inject.Inject

class GetDiscountUseCase @Inject constructor(
    private val discountRepository: DiscountRepository
) {
    suspend operator fun invoke(code: String): Result<Discount, DataError> {
        if (code.isBlank()) {
            return Result.Error(DataError.Discount.EMPTY)
        }
        return discountRepository.getDiscountByCode(code)
    }
}