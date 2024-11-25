package com.example.hammami.domain.usecase.giftcard

import com.example.hammami.data.repositories.GiftCardRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.core.result.Result
import javax.inject.Inject

class GetGiftCardByTransactionUseCase @Inject constructor(
    private val giftCardRepository: GiftCardRepository
) {
    suspend operator fun invoke(transactionId: String): Result<GiftCard, DataError> {
        return giftCardRepository.getGiftCardByTransactionId(transactionId)
    }
}