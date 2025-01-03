package com.example.hammami.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookingOld(
    @get:PropertyName("NomeServizio") @set:PropertyName("NomeServizio")   var name: String,
    @get:PropertyName("Prezzo") @set:PropertyName("Prezzo") var price: Float?,
    //@get:PropertyName("Data") @set:PropertyName("Data") var date: Timestamp,
    @get:PropertyName("Giorno") @set:PropertyName("Giorno") var day: Int,
    @get:PropertyName("Mese") @set:PropertyName("Mese") var month: Int,
    @get:PropertyName("Ora") @set:PropertyName("Ora")   var hour: Int,
    @get:PropertyName("Minuto") @set:PropertyName("Minuto") var minute: Int,
    @get:PropertyName("Cliente") @set:PropertyName("Cliente")   var email: String,
    @get:PropertyName("isExpired") @set:PropertyName("isExpired")   var isExpired: Boolean = false
    ) : Parcelable {
    constructor() : this("", null,0, 0, 0, 0, "", false)
}