package com.example.hammami.domain.error

sealed interface DataError : Error {

    enum class Firestore : DataError {
        UNKNOWN,
    }

    enum class Unknown : DataError {
        UNKNOWN,
    }

    enum class Network : DataError {
        NO_INTERNET,
        UNKNOWN,
        SERVER_ERROR,
        OPERATION_CANCELLED,
        SERVICE_UNAVAILABLE,
    }

    enum class Auth : DataError {
        USER_NOT_FOUND,
        TOKEN_REFRESH_FAILED,
        INVALID_CREDENTIALS,
        EMAIL_ALREADY_IN_USE,
        REQUIRED_PASSWORD,
        WEAK_PASSWORD,
        NOT_AUTHENTICATED,
        UNKNOWN

    }

    enum class User : DataError {
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        PERMISSION_DENIED,
        UPDATE_FAILED,
        INVALID_INPUT,
        INSUFFICIENT_POINTS,
        COUPON_NOT_FOUND,
        COUPON_ALREADY_EXISTS,
        COUPON_EXPIRED,
        COUPON_ALREADY_USED,
        GIFT_CARD_NOT_FOUND,
        GIFT_CARD_NOT_VALID,
        UNKNOWN
    }

    enum class Storage : DataError {
        BUCKET_NOT_FOUND,
        QUOTA_EXCEEDED,
        UPLOAD_FAILED,
    }

    enum class Payment : DataError {
        INVALID_DISCOUNT_CODE,
        INVALID_AMOUNT,
        UNKNOWN,
        DISCOUNT_EXCEEDS_AMOUNT,
        INVALID_PAYMENT_INFO,
    }

    enum class Voucher : DataError {
        NOT_FOUND,
        ALREADY_EXISTS,
        VALUE_EXCEEDS_AMOUNT,
        PERMISSION_DENIED,
        INSUFFICIENT_POINTS,
        EXPIRED,
    }

    enum class Service : DataError {
        SERVICE_NOT_FOUND,
    }

    enum class Booking : DataError {
        INVALID_TIME,
        INVALID_DATE,
        NO_OPERATORS_AVAILABLE,
        SLOT_NOT_AVAILABLE,
    }
}