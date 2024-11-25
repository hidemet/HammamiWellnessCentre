package com.example.hammami.domain.model.payment

import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.domain.model.giftCard.GiftCard
import java.time.LocalDateTime

sealed class Discount {
    abstract val code: String
    abstract val value: Double
    abstract fun isValid(): Boolean
    abstract fun canBeAppliedTo(amount: Double): Boolean

    data class GiftCardDiscount(
        override val code: String,
        override val value: Double,
        val expirationDate: LocalDateTime,
        val isUsed: Boolean
    ) : Discount() {
        override fun isValid(): Boolean = !isUsed &&
                expirationDate.isAfter(LocalDateTime.now())

        override fun canBeAppliedTo(amount: Double): Boolean =
            amount >= value
    }

    data class CouponDiscount(
        override val code: String,
        override val value: Double,
        val expirationDate: LocalDateTime,
        val isUsed: Boolean,
        val userId: String
    ) : Discount() {
        override fun isValid(): Boolean = !isUsed &&
                expirationDate.isAfter(LocalDateTime.now())

        override fun canBeAppliedTo(amount: Double): Boolean =
            value >= amount
    }
}
