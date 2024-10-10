package com.example.hammami.presentation.ui.fragments.loginResigter

import android.util.Log
import com.example.hammami.core.ui.UiText
import com.example.hammami.model.User

data class RegistrationFormState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val allergies: String = "",
    val disabilities: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val friendCode: String = "",
    val firstNameError: UiText? = null,
    val lastNameError: UiText? = null,
    val birthDateError: UiText? = null,
    val genderError: UiText? = null,
    val phoneNumberError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null
) {
    fun toUser() = User(
        firstName = firstName,
        lastName = lastName,
        birthDate = birthDate,
        gender = gender,
        email = email,
        phoneNumber = phoneNumber,
        allergies = allergies,
        disabilities = disabilities
    ).also {
        Log.d("RegistrationFormData", "User object created: $it")
    }
}