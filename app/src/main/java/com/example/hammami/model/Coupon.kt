package com.example.hammami.model

import com.google.firebase.Timestamp

data class Coupon(
    val code: String = "",
    val value: Int = 0,
    val expirationDate: Timestamp = Timestamp.now()
)