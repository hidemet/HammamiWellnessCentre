package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.Date

enum class VoucherType {
    GIFT_CARD,
    COUPON
}

data class Voucher(
    val id: String = "",
    val code: String = "",
    val value: Double = 0.0,
    val type: VoucherType = VoucherType.GIFT_CARD,
    val createdAt: Timestamp = Timestamp.now(),
    val expirationDate: Timestamp = Timestamp.now(),
    val creationTransactionId: String? = null,
    val createdBy: String  = "" // userId di chi l'ha creato/acquistato
) {
    @get:Exclude
    val isExpired: Boolean
        get() = expirationDate.toDate().after(Date())
}