package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.Date

enum class VoucherType {
    GIFT_CARD,
    COUPON
}

data class Voucher(
    val userId: String = "",
    val code: String = "",
    val value: Double = 0.0,
    val type: VoucherType = VoucherType.GIFT_CARD,
    val createdAt: Timestamp = Timestamp.now(),
    val expirationDate: Timestamp = Timestamp.now(),
    val creationTransactionId: String? = null
) {
    @get:Exclude
    val isExpired: Boolean
        get() = expirationDate.toDate().before(Date())
}