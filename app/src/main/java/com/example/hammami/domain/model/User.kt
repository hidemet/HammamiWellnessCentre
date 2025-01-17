package com.example.hammami.domain.model

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
    val role: String = "client"
) {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_CLIENT = "client"
    }
}
