package com.example.hammami.domain.usecase.giftcard

import com.example.hammami.data.repositories.GiftCardRepository
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class CreateGiftCardUseCase @Inject constructor(
    private val giftCardRepository: GiftCardRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        value: Double,
        transactionId: String,
    ): Result<GiftCard, DataError> {
        // Verifica che l'utente sia autenticato
        val userIdResult = authRepository.getCurrentUserId()
        if (userIdResult is Result.Error) {
            return Result.Error(DataError.Auth.NOT_AUTHENTICATED)
        }
        val userId = (userIdResult as Result.Success).data

        return giftCardRepository.createGiftCard(
            value = value,
            purchaserId = userId,
            transactionId = transactionId,
        )
    }
}