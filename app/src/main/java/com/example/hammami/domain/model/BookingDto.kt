package com.example.hammami.domain.model

import com.google.firebase.Timestamp


data class BookingDto(
    var id: String? = null,
    var serviceId: String? = null,
    var userId: String? = null,
    var serviceName: String? = null,
    var dateMillis: Long? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var status: String? = null,
    var creationTimestamp: Timestamp? = null,
    var reservationTimestamp: Timestamp? = null,
    var transactionId: String? = null,
) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)

    // Metodo per mappare il Booking
    fun toBooking(): Booking {
        return Booking(
            id = id ?: throw IllegalArgumentException("L'id non può essere nullo"),
            serviceId = serviceId ?: "",
            userId = userId ?: "",
            serviceName = serviceName ?: "",
            dateMillis = dateMillis ?: throw IllegalArgumentException("La data non può essere nulla"),
            startTime = startTime ?: "",
            endTime = endTime ?: "",
            status = BookingStatus.valueOf(status ?: BookingStatus.RESERVED.name),
            creationTimestamp = creationTimestamp ?: Timestamp.now(),
            reservationTimestamp = reservationTimestamp ?: Timestamp.now(),
            transactionId = transactionId,
        )
    }
}