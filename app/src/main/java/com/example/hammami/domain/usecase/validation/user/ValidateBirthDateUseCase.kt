package com.example.hammami.domain.usecase.validation.user

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.example.hammami.domain.error.ValidationError.User.BirthDateError
import com.example.hammami.core.result.Result


class ValidateBirthDateUseCase {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    operator fun invoke(birthDate: String): Result<Unit, BirthDateError> {
        return try {
            val date = LocalDate.parse(birthDate, dateFormatter)
            when {
                date.isAfter(LocalDate.now()) ->
                    Result.Error(BirthDateError.FUTURE_DATE)

                date.isBefore(LocalDate.now().minusYears(120)) ->
                    Result.Error(BirthDateError.TOO_OLD)

                else -> Result.Success(Unit)
            }
        } catch (e: DateTimeParseException) {
            Result.Error(BirthDateError.INVALID_FORMAT)
        }
    }
}




