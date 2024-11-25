package com.example.hammami.domain.usecase.giftcard

import com.example.hammami.data.repositories.GiftCardRepository
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result

import javax.inject.Inject


class ValidateGiftCardUseCase @Inject constructor(
    private val giftCardRepository: GiftCardRepository
) {
    suspend operator fun invoke(code: String, servicePrice: Double): Result<GiftCard, DataError> {
        if (code.isBlank()) {
            return Result.Error(DataError.GiftCard.VALIDATION_FAILED)
        }
        return giftCardRepository.getValidatedGiftCard(code, servicePrice)
    }
}