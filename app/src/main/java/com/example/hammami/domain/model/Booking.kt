package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Parcelize
data class Booking(
    var id: String,
    val serviceId: String,
    val userId: String,
    val serviceName: String,
    val dateMillis: Long,
    val startTime: String, // formato: "HH:mm"
    val endTime: String, // formato: "HH:mm"
    val status: BookingStatus = BookingStatus.RESERVED,
    val creationTimestamp: Timestamp = Timestamp.now(),
    val reservationTimestamp: Timestamp = Timestamp.now(),
    val transactionId: String? = null,
    val price: Double,
) : Parcelable

enum class BookingStatus {
    RESERVED,
    CONFIRMED,
}

val Booking.localDate: LocalDate?
    get() = this.dateMillis.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }

fun Booking.withLocalDate(localDate: LocalDate): Booking {
    return this.copy(dateMillis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
}