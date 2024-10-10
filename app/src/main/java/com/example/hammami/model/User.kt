package com.example.hammami.model


data class User(
    var firstName: String = "",
    var lastName: String = "",
    var birthDate: String = "",
    var gender: String = "",
    var allergies: String = "",
    var disabilities: String = "",
    var phoneNumber: String = "",
    var email: String = "",
    var profileImage: String = "",
    var points: Int = 0,
    var coupons: List<Coupon> = emptyList()
)
