package com.example.hammami.domain.usecase.giftcard

import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.GiftCardRepository
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class GetUserGiftCardsUseCase @Inject constructor(
    private val giftCardRepository: GiftCardRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<GiftCard>, DataError> {
        return when (val userResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                giftCardRepository.getGiftCardsByUser(userResult.data)
            }
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}