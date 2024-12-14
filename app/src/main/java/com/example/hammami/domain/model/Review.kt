package com.example.hammami.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    //@get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("Commento") @set:PropertyName("Commento") var commento: String = "",
    @get:PropertyName("Utente") @set:PropertyName("Utente") var utente: String = "",
    @get:PropertyName("Valutazione") @set:PropertyName("Valutazione") var valutazione: Float = 0f,
): Parcelable {
    constructor() : this("", "", 0f)
}