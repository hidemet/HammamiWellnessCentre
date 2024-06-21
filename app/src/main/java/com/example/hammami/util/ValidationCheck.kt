package com.example.hammami.util

// creare da file serve a non ripetere le funzioni su i vari fragments
fun String.validateEmail(): RegisterValidation {
    return when {
        isEmpty() -> RegisterValidation.Failed("L'email è obbligatoria")
        !android.util.Patterns.EMAIL_ADDRESS.matcher(this)
            .matches() -> RegisterValidation.Failed("Email non valida")

        else -> RegisterValidation.Success
    }
}

fun String.validatePassword(): RegisterValidation {
    return when {
        isEmpty() -> RegisterValidation.Failed("La password è obbligatoria")
        length < 6 -> RegisterValidation.Failed("La password deve contenere almeno 6 caratteri")
        else -> RegisterValidation.Success
    }
}