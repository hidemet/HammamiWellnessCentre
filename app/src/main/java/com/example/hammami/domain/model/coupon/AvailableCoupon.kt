package com.example.hammami.domain.model.coupon

data class AvailableCoupon(
    val value: Double,
    val requiredPoints: Int,
    val description: String = "Sconto di $value€",
    val isEnabled: Boolean = true
)  {
    fun canBeRedeemed(userPoints: Int): Boolean =
        isEnabled && userPoints >= requiredPoints
}