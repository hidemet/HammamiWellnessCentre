package com.example.hammami.data

data class Service (
    val id: String,
    val name: String,
    val price: Float,
    val discountPrice: Float? = null,
    val description: String,
    val image: String,
    val reviews: List<String>
)