package com.example.hammami.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName

data class Service(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("Nome") @set:PropertyName("Nome") var name: String = "",
    @get:PropertyName("Prezzo") @set:PropertyName("Prezzo") var price: Float? = null,
    @get:PropertyName("PrezzoScontato") @set:PropertyName("PrezzoScontato") var discountPrice: Float? = null,
    @get:PropertyName("Descrizione") @set:PropertyName("Descrizione") var description: String = "",
    @get:PropertyName("Immagine") @set:PropertyName("Immagine") var image: DocumentReference? = null,
    @get:PropertyName("Durata") @set:PropertyName("Durata") var length: Long? = null,
    @get:PropertyName("Categoria") @set:PropertyName("Categoria") var category: List<String>? = null,
    @get:PropertyName("Recensioni") @set:PropertyName("Recensioni") var reviews: List<DocumentReference>? = null,
    @get:PropertyName("Sezione homepage") @set:PropertyName("Sezione homepage") var homepageSection: String? = null,
    @get:PropertyName("Benefici") @set:PropertyName("Benefici") var benefits: String? = null
) {
    constructor() : this("", "", null, null, "", null, null, null, null, null, null)
}