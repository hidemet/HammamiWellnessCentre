package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import android.os.Parcelable
import com.example.hammami.data.entity.BookingDto
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Parcelize
data class Booking(
    val id: String,
    val serviceId: String,
    val userId: String,
    val serviceName: String,
    val startDate: Timestamp, // Modifica
    val endDate: Timestamp,   // Modifica
    val status: BookingStatus = BookingStatus.RESERVED,
    val creationTimestamp: Timestamp = Timestamp.now(),
    val reservationTimestamp: Timestamp = Timestamp.now(),
    val transactionId: String? = null,
    val hasReview: Boolean = false,
    val price: Double,
) : Parcelable

enum class BookingStatus {
    RESERVED,
    CONFIRMED,
    COMPLETED,
    CANCELED
}