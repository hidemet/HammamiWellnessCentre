package com.example.hammami.domain.usecase.giftcard

import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.giftCard.AvailableGiftCard

import javax.inject.Inject


class GetAvailableGiftCardsUseCase @Inject constructor() {
     operator fun invoke(): Result<List<AvailableGiftCard>, DataError> {
        return Result.Success(
            AvailableGiftCard.PREDEFINED_VALUES.map { value ->
                AvailableGiftCard(value = value, isEnabled = true)
            }
        )
    }
}
