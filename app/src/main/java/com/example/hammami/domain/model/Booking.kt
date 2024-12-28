package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Booking(
    var id: String? = null,
    val serviceId: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val date: String? = null,
    val startTime: String = "", // formato: "HH:mm"
    val endTime: String = "", // formato: "HH:mm"
    val status: BookingStatus = BookingStatus.RESERVED,
    val creationTimestamp: Timestamp = Timestamp.now(),
    val reservationTimestamp: Timestamp = Timestamp.now(),
    val transactionId: String? = null
) : Parcelable

enum class BookingStatus {
    RESERVED,
    CONFIRMED,
}