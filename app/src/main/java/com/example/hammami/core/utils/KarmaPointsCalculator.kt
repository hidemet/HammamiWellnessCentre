package com.example.hammami.core.utils

import com.example.hammami.domain.model.payment.PaymentItem
import javax.inject.Inject

class KarmaPointsCalculator @Inject constructor() {
    companion object {
        private const val POINTS_PER_EURO = 0.1 // 1 punto ogni 10€
        private const val GIFT_CARD_POINTS_MULTIPLIER = 1.5 // +50% punti per acquisto gift card
    }

    fun calculatePoints(
        amount: Double,
        item: PaymentItem
    ): Int {
        val basePoints = (amount * POINTS_PER_EURO).toInt()

        return when (item) {
            is PaymentItem.GiftCardPayment->
                (basePoints * GIFT_CARD_POINTS_MULTIPLIER).toInt()
            else -> basePoints
        }
    }
}