package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Booking(
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val date: String = "", // formato: "yyyy-MM-dd"
    val startTime: String = "", // formato: "HH:mm"
    val endTime: String = "", // formato: "HH:mm"
    val operator: Int = 0,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val createdAt: Timestamp = Timestamp.now(),
    val price: Double = 0.0
) : Parcelable

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}