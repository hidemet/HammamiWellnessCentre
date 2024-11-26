package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import java.util.Date

enum class VoucherType {
    GIFT_CARD,    // Acquistato con denaro
    COUPON  // Acquisto con punti
}

data class DiscountVoucher(
    val id: String = "",
    val code: String,
    val value: Double,
    val type: VoucherType,
    val createdAt: Timestamp = Timestamp.now(),
    val expirationDate: Timestamp,
    val creationTransactionId: String? = null,  // ID della transazione di acquisto/riscatto
    val createdBy: String  // userId di chi l'ha creato/acquistato
) {
    fun isValid(): Boolean = !isExpired()
    fun isExpired(): Boolean = expirationDate.toDate().before(Date())

    companion object {
        fun generateCode(value: Double, type: VoucherType): String {
            val prefix = when(type) {
                VoucherType.GIFT_CARD -> "GC"
                VoucherType.COUPON -> "CO"
            }
            val timestamp = System.currentTimeMillis().toString().takeLast(6)
            val random = (1..8).map { ('A'..'Z') + ('0'..'9').random() }.joinToString("")
            return "$prefix$timestamp$random${value.toInt()}"
        }
    }
}