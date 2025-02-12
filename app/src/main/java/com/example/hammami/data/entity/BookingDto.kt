package com.example.hammami.data.entity
import com.google.firebase.Timestamp


data class BookingDto(
    var id: String? = null,
    var serviceId: String? = null,
    var userId: String? = null,
    var serviceName: String? = null,
    var startDate: Timestamp? = null,
    var endDate: Timestamp? = null,
    var status: String? = null,
    var creationTimestamp: Timestamp? = null,
    var reservationTimestamp: Timestamp? = null,
    var transactionId: String? = null,
    val hasReview: Boolean = false,
    val price: Double? = null,
    )