package com.example.hammami.domain.model.payment

import android.os.Parcelable
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.model.Service
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
sealed class PaymentItem : Parcelable {
    abstract val price: Double

    @Parcelize
    data class ServiceBookingPayment(
        override val price: Double,
        val title: String,
        val description: String,
        val serviceId: String,
        val dateTime: LocalDateTime,
        val duration: Int
    ) : PaymentItem()

    @Parcelize
    data class GiftCardPayment(
        override val price: Double
    ) : PaymentItem()
}

fun Service.toPaymentItem(dateTime: LocalDateTime) = PaymentItem.ServiceBookingPayment(
    price = price!!.toDouble(),
    title = name,
    description = description,
    serviceId = id,
    dateTime = dateTime,
    duration = length?.toInt() ?: 0
)

fun AvailableVoucher.toPaymentItem() = PaymentItem.GiftCardPayment(
    price = value
)

