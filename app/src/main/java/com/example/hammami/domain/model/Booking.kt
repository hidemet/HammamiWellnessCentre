package com.example.hammami.domain.model

import com.google.firebase.Timestamp

data class Booking(
    val id: String = "",
    val userId: String = "",
    val treatmentId: String = "",
    val date: Timestamp = Timestamp.now(),
    val price: Double = 0.0,
    val finalPrice: Double? = null,
    val appliedCouponId: String? = null,
    val status: BookingStatus = BookingStatus.PENDING
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}