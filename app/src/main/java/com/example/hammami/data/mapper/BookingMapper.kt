package com.example.hammami.data.mapper

import com.example.hammami.data.entity.BookingDto
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.BookingStatus
import com.google.firebase.Timestamp
import javax.inject.Inject

class BookingMapper @Inject constructor(){
    fun toDto(booking: Booking): BookingDto {
        return BookingDto(
            id = booking.id,
            serviceId = booking.serviceId,
            userId = booking.userId,
            serviceName = booking.serviceName,
            startDate = booking.startDate,
            endDate = booking.endDate,
            status = booking.status.name,
            creationTimestamp = booking.creationTimestamp,
            reservationTimestamp = booking.reservationTimestamp,
            transactionId = booking.transactionId,
            hasReview = booking.hasReview,
            price = booking.price
        )
    }

    fun toDomain(bookingDto: BookingDto): Booking {
        return Booking(
            id = bookingDto.id ?: throw IllegalArgumentException("ID cannot be null"),
            serviceId = bookingDto.serviceId ?: "",
            userId = bookingDto.userId ?: "",
            serviceName = bookingDto.serviceName ?: "",
            startDate = bookingDto.startDate ?: throw IllegalArgumentException("startDate cannot be null"),
            endDate = bookingDto.endDate ?: throw IllegalArgumentException("endDate cannot be null"),
            status = BookingStatus.valueOf(bookingDto.status ?: BookingStatus.RESERVED.name),
            creationTimestamp = bookingDto.creationTimestamp ?: Timestamp.now(),
            reservationTimestamp = bookingDto.reservationTimestamp ?: Timestamp.now(),
            transactionId = bookingDto.transactionId,
            hasReview = bookingDto.hasReview,
            price = bookingDto.price ?: 0.0
        )
    }
}