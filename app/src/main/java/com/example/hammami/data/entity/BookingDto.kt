package com.example.hammami.data.entity

import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.google.firebase.Timestamp


data class BookingDto(
    var id: String? = null,
    var serviceId: String? = null,
    var userId: String? = null,
    var serviceName: String? = null,
//    var dateMillis: Long? = null,
//    var startTime: String? = null,
//    var endTime: String? = null,
    var startDate: Timestamp? = null, // Modifica
    var endDate: Timestamp? = null,   // Modifica
    var status: String? = null,
    var creationTimestamp: Timestamp? = null,
    var reservationTimestamp: Timestamp? = null,
    var transactionId: String? = null,
    val hasReview: Boolean = false,
    val price: Double? = null,
    ) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null)
}