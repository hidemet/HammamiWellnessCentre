package com.example.hammami.domain.model.giftCard

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.security.SecureRandom
import java.util.Date
import java.util.concurrent.TimeUnit


data class GiftCard(
    val id: String = "",
    val code: String = "",
    val value: Double = 0.0,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val expirationDate: Timestamp = Timestamp.now(),
    val isUsed: Boolean = false,
    val usedDate: Timestamp? = null,
    val transactionId: String? = null
) {
    fun isValid(): Boolean = !isUsed && !isExpired()
    fun isExpired(): Boolean = expirationDate.toDate().before(Date())
    fun canBeAppliedTo(amount: Double): Boolean = value <= amount

    companion object {
        fun generateCode(value: Double): String {
            val random = SecureRandom()
            val timestamp = System.currentTimeMillis()
            val chars = ('A'..'Z') + ('0'..'9')
            val randomPart = (1..8).map { chars[random.nextInt(chars.size)] }.joinToString("")
            return "GC${timestamp.toString().takeLast(6)}$randomPart${value.toInt()}"
        }
    }
}