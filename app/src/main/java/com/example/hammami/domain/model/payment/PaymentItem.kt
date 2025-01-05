package com.example.hammami.domain.model.payment

import android.os.Parcelable
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.model.Service
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
sealed class PaymentItem : Parcelable {
    abstract val price: Double

    @Parcelize
    data class ServiceBookingPayment(
        override val price: Double,
        val serviceName: String,
        val bookingId: String,
        val date: LocalDate,
        val startTime: String,
        val endTime: String,
        val discountCode: String? = null // Aggiunto
    ) : PaymentItem() {
        companion object {
            fun from(
                service: Service,
                bookingId: String,
                date: LocalDate,
                startTime: String,
                endTime: String,
                price: Double
            ): ServiceBookingPayment = ServiceBookingPayment(
                price = price,
                serviceName = service.name,
                bookingId = bookingId,
                date = date,
                startTime = startTime,
                endTime = endTime
            )
        }
    }

    @Parcelize
    data class GiftCardPayment(
        override val price: Double,
        val discountCode: String? = null //
    ) : PaymentItem() {
        companion object {
            fun from(voucher: AvailableVoucher): GiftCardPayment = GiftCardPayment(
                price = voucher.value
            )
        }
    }
}

