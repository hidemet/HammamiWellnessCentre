package com.example.hammami.domain.usecase

sealed interface DataError : Error {
    enum class Network: DataError {
        NO_INTERNET,
        UNKNOWN,
        SERVER_ERROR
    }

    enum class Auth: DataError {
        USER_NOT_FOUND,
        TOKEN_REFRESH_FAILED,
        INVALID_CREDENTIALS,
        EMAIL_ALREADY_IN_USE,
        WEAK_PASSWORD,
        NOT_AUTHENTICATED,
        UNKNOWN

    }

    enum class User: DataError {
        USER_NOT_FOUND,
        USER_ALREADY_EXISTS,
        PERMISSION_DENIED,
        UPDATE_FAILED,

    }

    enum class Storage: DataError {
        BUCKET_NOT_FOUND,
        QUOTA_EXCEEDED,
        UPLOAD_FAILED
    }


}