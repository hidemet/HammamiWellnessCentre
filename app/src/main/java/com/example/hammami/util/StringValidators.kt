package com.example.hammami.util

import android.util.Log
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

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

//    val BirthDate = object : Validator<Triple<String, String, String>> {
//        override fun validate(value: Triple<String, String, String>): ValidationResult {
//            val (day, month, year) = value
//            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)
//
//            return try {
//                val date = LocalDate.parse("$day $month $year", formatter)
//                when {
//                    date.isAfter(LocalDate.now()) ->
//                        ValidationResult.Invalid("La data di nascita non può essere nel futuro")
//                    else -> ValidationResult.Valid
//                }
//            } catch (e: DateTimeParseException) {
//                ValidationResult.Invalid("Inserisci una data di nascita valida")
//            } catch (e: Exception) {
//                ValidationResult.Invalid("Errore nella validazione della data di nascita")
//            }
//        }
//    }


    val BirthDate = object : Validator<Triple<String, String, String>> {
        override fun validate(value: Triple<String, String, String>): ValidationResult {
            val (day, month, year) = value

            Log.d("BirthDateValidator", "Validating date: $day $month $year")

            return try {

                val italianLocale = Locale("it", "IT")
                val monthFormatter = DateTimeFormatter.ofPattern("MMMM", italianLocale)

                val parsedDay = day.toInt()
                val parsedMonth = Month.from(monthFormatter.parse(month))
                val parsedYear = year.toInt()

                val date = LocalDate.of(parsedYear, parsedMonth, parsedDay)

                Log.d("BirthDateValidator", "Parsed date: $date")
                when {
                    date.isAfter(LocalDate.now()) -> {
                        Log.d("BirthDateValidator", "Date is in the future")
                        ValidationResult.Invalid("La data di nascita non può essere nel futuro")
                    }

                    else -> {
                        Log.d("BirthDateValidator", "Date is valid")
                        ValidationResult.Valid
                    }
                }
            } catch (e: DateTimeParseException) {
                Log.e("BirthDateValidator", "DateTimeParseException: ${e.message}")
                ValidationResult.Invalid("Inserisci una data di nascita valida")
            } catch (e: Exception) {
                Log.e("BirthDateValidator", "Exception: ${e.message}")
                ValidationResult.Invalid("Errore nella validazione della data di nascita")
            }
        }

    }
}