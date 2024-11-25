package com.example.hammami.domain.model.coupon

import com.google.firebase.Timestamp
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

data class Coupon(
    val id: String = "",
    val code: String = "",
    val value: Double = 0.0,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val expirationDate: Timestamp = Timestamp.now(),
    val usedInBooking: String? = null,
    val isUsed: Boolean = false,
    val usedDate: Timestamp? = null
) {
    fun isValid(): Boolean = !isUsed && !isExpired()
    fun isExpired(): Boolean = expirationDate.toDate().before(Date())
}

fun Coupon.getFormattedValue(): String = String.format("%.2f€", value)
fun Coupon.getFormattedExpirationDate(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(expirationDate.toDate())