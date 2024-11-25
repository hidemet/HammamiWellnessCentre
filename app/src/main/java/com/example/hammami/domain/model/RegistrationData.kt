package com.example.hammami.domain.model

data class RegistrationData(
    var firstName: String = "",
    var lastName: String = "",
    val birthDate: String = "",
    var gender: String = "",
    var allergies: String = "",
    var disabilities: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var profileImage: String = ""
) {

    fun toUser() = User(
        firstName = firstName,
        lastName = lastName,
        birthDate = birthDate,
        gender = gender,
        allergies = allergies,
        disabilities = disabilities,
        email = email,
        phoneNumber = phoneNumber,
        profileImage = profileImage,
        points = 0
    )
}