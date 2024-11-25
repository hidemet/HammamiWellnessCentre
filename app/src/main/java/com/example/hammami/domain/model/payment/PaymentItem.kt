package com.example.hammami.domain.model.payment

import android.os.Parcelable
import com.example.hammami.domain.model.giftCard.AvailableGiftCard
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
@Parcelize
sealed class PaymentItem: Parcelable {
    abstract val id: String
    abstract val amount: Double
    abstract val title: String
    abstract val description: String

    @Parcelize
    data class ServiceBooking(
        override val id: String,
        override val amount: Double,
        override val title: String,
        override val description: String,
        val dateTime: LocalDateTime,
        val duration: Int,
        val serviceId: String
    ) : PaymentItem()

    @Parcelize
    data class GiftCardPurchase(
        override val id: String = UUID.randomUUID().toString(),
        val value: Double,
        override val amount: Double = value,
        override val title: String = "Gift Card da ${value}€",
        override val description: String = "Acquisto Gift Card del valore di ${value}€",
        val recipientEmail: String? = null
    ) : PaymentItem(), Parcelable {

        companion object {
            fun fromAvailableGiftCard(giftCard: AvailableGiftCard): PaymentItem.GiftCardPurchase {
                return PaymentItem.GiftCardPurchase(
                    id = UUID.randomUUID().toString(),
                    value = giftCard.value
                )
            }
        }
    }
}
