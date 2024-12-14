package com.example.hammami.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import javax.inject.Inject

@Parcelize
data class Service(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("Nome") @set:PropertyName("Nome") var name: String = "",
    @get:PropertyName("Prezzo") @set:PropertyName("Prezzo") var price: Float? = null,
    @get:PropertyName("Prezzo scontato") @set:PropertyName("Prezzo scontato") var discountPrice: Float? = null,
    @get:PropertyName("Descrizione") @set:PropertyName("Descrizione") var description: String = "",
    @get:PropertyName("Immagine") @set:PropertyName("Immagine") var image: String? = null,
    @get:PropertyName("Durata") @set:PropertyName("Durata") var length: Long? = null,
    @get:PropertyName("Categoria") @set:PropertyName("Categoria") var category: String? = null,
    @get:PropertyName("Recensioni") @set:PropertyName("Recensioni") var reviews: @RawValue List<DocumentReference>? = null,   // List<DocumentReference>
    @get:PropertyName("Sezione homepage") @set:PropertyName("Sezione homepage") var homepageSection: String? = null,
    @get:PropertyName("Benefici") @set:PropertyName("Benefici") var benefits: String? = null
) : Parcelable{
    constructor() : this("", "", null, null, "", null, null, null, null, null, null)
}