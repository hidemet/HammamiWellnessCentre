package com.example.hammami.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (

    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val imagePath:String="",

    ): Parcelable {
    constructor():this("","","","")
}