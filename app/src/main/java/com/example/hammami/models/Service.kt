package com.example.hammami.data

data class Service (
    val id: String,
    val name: String,
    val price: String,
    val discountPrice: Float? = null,
    val description: String,
    val image: String,
    val reviews: List<String>? = null,
    val homepageSection: String? = null
){
    constructor():this("", "", "", null, "", "", null, null)
}

