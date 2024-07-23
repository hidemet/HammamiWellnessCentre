package com.example.hammami.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object StringValidators {
    val NotBlank = object : Validator<String> {
        override fun validate(value: String) = when {
            value.isBlank() -> ValidationResult.Invalid("Il campo non può essere vuoto")
            else -> ValidationResult.Valid
        }
    }

    val Email = object : Validator<String> {
        override fun validate(value: String) = when {
            android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> ValidationResult.Valid
            else -> ValidationResult.Invalid("Inserisci una email valida")
        }
    }

    val PhoneNumber = object : Validator<String> {
        private val phoneRegex = "^\\d{10}$".toRegex()
        override fun validate(value: String) = when {
            phoneRegex.matches(value) -> ValidationResult.Valid
            else -> ValidationResult.Invalid("Inserisci un numero di telefono valido")
        }
    }

    val Password = object : Validator<String> {
        private val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
        override fun validate(value: String) = when {
            passwordRegex.matches(value) -> ValidationResult.Valid
            else -> ValidationResult.Invalid("Inserisci una password valida (La password deve essere lunga almeno 8 caratteri e contenere almeno una lettera e un numero)")
        }
    }

    val BirthDate = object : Validator<Triple<String, String, String>> {
        override fun validate(value: Triple<String, String, String>): ValidationResult {
            val (day, month, year) = value
            return try {
                val date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
                when {
                    date.isAfter(LocalDate.now()) ->
                        ValidationResult.Invalid("La data di nascita non può essere nel futuro")
                    else -> ValidationResult.Valid
                }
            } catch (e: DateTimeParseException) {
                ValidationResult.Invalid("Inserisci una data di nascita valida")
            } catch (e: NumberFormatException) {
                ValidationResult.Invalid("Inserisci una data di nascita completa")
            }
        }
    }


}