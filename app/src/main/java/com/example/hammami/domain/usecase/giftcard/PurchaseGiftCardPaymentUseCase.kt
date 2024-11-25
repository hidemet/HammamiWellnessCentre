package com.example.hammami.domain.usecase.giftcard

import com.example.hammami.domain.model.payment.PaymentSystem
import com.example.hammami.domain.usecase.payment.ProcessPaymentUseCase
import kotlinx.coroutines.coroutineScope
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class PurchaseGiftCardUseCase @Inject constructor(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val createGiftCardUseCase: CreateGiftCardUseCase
) {
    suspend operator fun invoke(
        paymentSystem: PaymentSystem,
        value: Double,
        recipientEmail: String? = null
    ): Result<GiftCardPurchaseResult, DataError> {
        return when (val paymentResult = processPaymentUseCase(value, paymentSystem)) {
            is Result.Success -> {
                val transactionId = paymentResult.data
                when (val giftCardResult = createGiftCardUseCase(
                    value = value,
                    transactionId = transactionId,
                )) {
                    is Result.Success -> Result.Success(
                        GiftCardPurchaseResult(
                            transactionId = transactionId,
                            giftCard = giftCardResult.data
                        )
                    )
                    is Result.Error -> Result.Error(giftCardResult.error)
                }
            }
            is Result.Error -> Result.Error(paymentResult.error)
        }
    }

    data class GiftCardPurchaseResult(
        val transactionId: String,
        val giftCard: GiftCard
    )
}
