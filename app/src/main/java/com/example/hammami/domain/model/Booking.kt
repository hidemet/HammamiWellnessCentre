package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@Parcelize
data class Booking(
    var id: String? = null,
    val serviceId: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val date: Long? = null,
    val startTime: String = "", // formato: "HH:mm"
    val endTime: String = "", // formato: "HH:mm"
    val status: BookingStatus = BookingStatus.RESERVED,
    val creationTimestamp: Timestamp = Timestamp.now(),
    val reservationTimestamp: Timestamp = Timestamp.now(),
    val transactionId: String? = null,
) : Parcelable {
    fun getLocalDate(): LocalDate? {
        return date?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }
}


enum class BookingStatus {
    RESERVED,
    CONFIRMED,
}