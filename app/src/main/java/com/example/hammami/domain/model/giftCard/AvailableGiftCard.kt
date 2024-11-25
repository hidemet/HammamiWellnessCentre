package com.example.hammami.domain.model.giftCard

import com.example.hammami.domain.model.payment.PaymentItem
import java.util.UUID

data class AvailableGiftCard(
    val value: Double,
    val isEnabled: Boolean = true
) {
    companion object {
        val PREDEFINED_VALUES = listOf(20.0, 50.0, 100.0, 150.0, 200.0)
    }

    fun toPaymentItem() = PaymentItem.GiftCardPurchase(
        id = UUID.randomUUID().toString(), // o altro sistema di ID
        value = value
    )
}
