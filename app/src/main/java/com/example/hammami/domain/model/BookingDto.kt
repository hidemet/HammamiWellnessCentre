package com.example.hammami.domain.model

import com.google.firebase.Timestamp
import java.util.Date

data class BookingDto(
    var id: String? = null,
    var serviceId: String? = null,
    var userId: String? = null,
    var serviceName: String? = null,
    var date: Date? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var status: String? = null,
    var creationTimestamp: Timestamp? = null,
    var reservationTimestamp: Timestamp? = null,
    var transactionId: String? = null,
    var operatorId: Int? = null
) {
    // Costruttore senza argomenti per Firebase
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null)

    // Metodo di mappatura a Booking
    fun toBooking(): Booking {
        return Booking(
            id = id,
            serviceId = serviceId ?: "",
            userId = userId ?: "",
            serviceName = serviceName ?: "",
            date = date,
            startTime = startTime ?: "",
            endTime = endTime ?: "",
            status = BookingStatus.valueOf(status ?: BookingStatus.RESERVED.name),
            creationTimestamp = creationTimestamp ?: Timestamp.now(),
            reservationTimestamp = reservationTimestamp ?: Timestamp.now(),
            transactionId = transactionId,
            operatorId = operatorId ?: 0
        )
    }
}