package com.example.hammami.util

// File: UserExtensions.kt

//
//import com.example.hammami.databinding.DialogEditPersonalInfoBinding
//import com.example.hammami.databinding.FragmentEditProfileBinding
//import com.example.hammami.databinding.FragmentProfileBinding
//import com.example.hammami.models.User
//
//// File: UserExtensions.kt
//
//
//fun User.populateFields(binding: FragmentProfileBinding) {
//    with(binding) {
//        userName.text = "$firstName $lastName"
//        userPoints.text = points
//        // Aggiungi qui altri campi presenti in fragment_profile, se necessario
//    }
//}
//
//fun User.populateFields(binding: FragmentEditProfileBinding) {
//    with(binding) {
//        firstName.text = this@populateFields.firstName
//        lastName.text = this@populateFields.lastName
//        birthDate.text = this@populateFields.birthDate
//        allergies.text = this@populateFields.allergies.orDash()
//        disabilities.text = this@populateFields.disabilities.orDash()
//        phoneNumberValue.text = this@populateFields.phoneNumber
//        emailAddressValue.text = this@populateFields.email
//    }
//}
//
//fun User.populateFields(binding: DialogEditPersonalInfoBinding) {
//    val (day, month, year) = birthDate.toFormattedDate()
//
//    with(binding) {
//        firstNameEditText.setText(this@populateFields.firstName)
//        lastNameEditText.setText(this@populateFields.lastName)
//        dayEditText.setText(day)
//        monthAutoCompleteTextView.setText(month, false)
//        yearEditText.setText(year)
//        genderAutoCompleteTextView.setText(this@populateFields.gender, false)
//        allergiesEditText.setText(this@populateFields.allergies)
//        disabilitiesEditText.setText(this@populateFields.disabilities)
//    }
//}
//
//// Funzioni di utilità
//
//fun String?.orDash() = if (isNullOrBlank()) "-" else this
//
