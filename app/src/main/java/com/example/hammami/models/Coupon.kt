package com.example.hammami.models

import java.time.LocalDateTime

data class Coupon(
    val id: String = "",
    val userId: String,
    val code: String,
    val value: Int,
    val creationDate: LocalDateTime,
    val expirationDate: LocalDateTime,
    val isActive: Boolean = true,
    val isRedeemed: Boolean = false,
    val redemptionDate: LocalDateTime? = null
)