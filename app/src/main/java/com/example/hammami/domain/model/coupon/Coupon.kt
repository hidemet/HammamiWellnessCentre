package com.example.hammami.domain.model.coupon

import com.google.firebase.Timestamp
import java.security.SecureRandom
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Coupon(
    val id: String = "",
    val code: String = "",
    val value: Double = 0.0,
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
            return "CO${timestamp.toString().takeLast(6)}$randomPart${value.toInt()}"
        }
    }
}