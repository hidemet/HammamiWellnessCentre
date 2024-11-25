package com.example.hammami.domain.model.giftCard

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class GiftCard(
    @DocumentId val id: String = "",
    @PropertyName("code") val code: String = "",
    @PropertyName("value") val value: Double = 0.0,
    @PropertyName("purchaserId") val purchaserId: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("expirationDate") val expirationDate: Timestamp = Timestamp.now(),
    @PropertyName("used") val used: Boolean = false,
    @PropertyName("usedDate") val usedDate: Timestamp? = null,
    @PropertyName("transactionId") val transactionId: String? = null

) {
    fun isValid(): Boolean = !used && !isExpired()
    fun isExpired(): Boolean = expirationDate.toDate().before(Date())
}

