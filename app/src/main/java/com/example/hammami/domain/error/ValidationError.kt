package com.example.hammami.domain.error

sealed interface ValidationError : Error {
    sealed interface Card : ValidationError {
        enum class ExpiryDateError : Error {
            EMPTY,
            INVALID_FORMAT,
            EXPIRED
        }
        enum class CvvError : Error {
            EMPTY,
            INVALID_FORMAT,
            INVALID_LENGTH
        }

        enum class NumberError: Error {
            EMPTY,
            INVALID_FORMAT,
            INVALID_CARD,
            INVALID_LENGTH
        }

    }

    sealed interface User : ValidationError {
        enum class FirstNameError : Error {
            EMPTY,
            TOO_SHORT
        }
        enum class LastNameError : Error {
            EMPTY
        }
        enum class EmailError : Error {
            EMPTY,
            INVALID_FORMAT
        }
        enum class PasswordError : Error {
            EMPTY,
            INVALID_FORMAT
        }
        enum class ConfirmedPasswordError : Error {
            PASSWORDS_DO_NOT_MATCH
        }
        enum class GenderError : Error {
            EMPTY
        }
        enum class BirthDateError : Error {
            FUTURE_DATE,
            TOO_OLD,
            INVALID_FORMAT
        }
        enum class PhoneNumberError : Error {
            EMPTY,
            INVALID_FORMAT
        }
    }

        enum class DiscountError : Error {
            INVALID_DISCOUNT,
            EXCEEDS_AMOUNT,
            EMPTY_CODE,
            NOT_FOUND,
        }

        enum class Payment : Error {
            INVALID_CREDIT_CARD
        }


}