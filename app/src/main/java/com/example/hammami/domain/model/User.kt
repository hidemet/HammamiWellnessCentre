package com.example.hammami.domain.model

import com.example.hammami.domain.model.coupon.Coupon


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
