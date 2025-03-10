package com.example.hammami.domain.model

data class AvailableVoucher(
    val value: Double,
    val type: VoucherType,
    val isEnabled: Boolean = true,
    val requiredPoints: Int? = null,
)
{



    fun canBeRedeemed(userPoints: Int): Boolean = when (type) {
        VoucherType.GIFT_CARD -> isEnabled
        VoucherType.COUPON -> isEnabled &&
                requiredPoints != null &&
                userPoints >= requiredPoints
    }


//    fun toPaymentItem() = PaymentItem.giftCardPurchase(
//        id = UUID.randomUUID().toString(),
//        value = value,
//        type = type
//    )

    companion object {
        private val VALID_VALUES = mapOf(
            VoucherType.GIFT_CARD to setOf(20.0, 50.0, 100.0, 150.0, 200.0),
            VoucherType.COUPON to setOf(10.0, 20.0, 30.0))

        private const val POINTS_MULTIPLIER = 5

        fun createGiftCardOptions() = VALID_VALUES[VoucherType.GIFT_CARD]
            ?.map { value ->
                AvailableVoucher(
                    value = value,
                    type = VoucherType.GIFT_CARD
                )
            } ?: emptyList()

        fun createCouponOptions(userPoints: Int) = VALID_VALUES[VoucherType.COUPON]
                ?.map { value ->
                    val requiredPoints = (value * POINTS_MULTIPLIER).toInt()
                    AvailableVoucher(
                        value = value,
                        type = VoucherType.COUPON,
                        requiredPoints = requiredPoints,
                        isEnabled = userPoints >= requiredPoints
                    )
                } ?: emptyList()
    }



}




