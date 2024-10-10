package com.example.hammami.core.util

import com.example.hammami.R
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Error
import com.example.hammami.domain.usecase.ValidateBirthDateUseCase.*
import com.example.hammami.domain.usecase.ValidateConfirmedPasswordUseCase.*
import com.example.hammami.domain.usecase.ValidateEmailUseCase.*
import com.example.hammami.domain.usecase.ValidateFirstNameUseCase.*
import com.example.hammami.domain.usecase.ValidateGenderUseCase.*
import com.example.hammami.domain.usecase.ValidateLastNameUseCase.*
import com.example.hammami.domain.usecase.ValidatePasswordUseCase.*
import com.example.hammami.domain.usecase.ValidatePhoneNumberUseCase.*
import com.example.hammami.core.ui.UiText

fun FirstNameError.asUiText(): UiText {
    return when (this) {
        FirstNameError.EMPTY -> UiText.StringResource(R.string.error_first_name_empty)
        FirstNameError.TOO_SHORT -> UiText.StringResource(R.string.error_first_name_too_short)
    }
}

fun LastNameError.asUiText(): UiText {
    return UiText.StringResource(R.string.error_last_name_empty)
}

fun BirthDateError.asUiText(): UiText {
    return when (this) {
        BirthDateError.FUTURE_DATE -> UiText.StringResource(R.string.error_birthdate_future)
        BirthDateError.TOO_OLD -> UiText.StringResource(R.string.error_birthdate_too_old)
        BirthDateError.INVALID_FORMAT -> UiText.StringResource(R.string.error_birthdate_invalid_format)
    }
}

fun GenderError.asUiText(): UiText {
    return UiText.StringResource(R.string.error_gender_empty)
}

fun EmailError.asUiText(): UiText {
    return when (this) {
        EmailError.EMPTY -> UiText.StringResource(R.string.error_email_empty)
        EmailError.INVALID_FORMAT -> UiText.StringResource(R.string.error_email_invalid_format)
    }
}

fun PhoneNumberError.asUiText(): UiText {
    return when (this) {
        PhoneNumberError.EMPTY -> UiText.StringResource(R.string.error_phone_empty)
        PhoneNumberError.INVALID_FORMAT -> UiText.StringResource(R.string.error_phone_invalid_format)
    }
}

fun ConfirmedPasswordError.asUiText(): UiText {
    return UiText.StringResource(R.string.error_password_mismatch)
}

fun PasswordError.asUiText(): UiText {
    return when (this) {
        PasswordError.EMPTY -> UiText.StringResource(R.string.error_password_empty)
        PasswordError.INVALID_FORMAT -> UiText.StringResource(R.string.error_password_invalid_format)
    }
}

fun DataError.asUiText(): UiText {
    return when (this) {
        is DataError.Network -> when (this) {
            DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
            DataError.Network.UNKNOWN -> UiText.StringResource(R.string.error_unknown_network)
            DataError.Network.SERVER_ERROR -> UiText.StringResource(R.string.error_server_error)
        }
        is DataError.Auth -> when (this) {
            DataError.Auth.USER_NOT_FOUND -> UiText.StringResource(R.string.error_user_not_found)
            DataError.Auth.INVALID_CREDENTIALS -> UiText.StringResource(R.string.error_invalid_credentials)
            DataError.Auth.EMAIL_ALREADY_IN_USE -> UiText.StringResource(R.string.error_email_already_in_use)
            DataError.Auth.WEAK_PASSWORD -> UiText.StringResource(R.string.error_weak_password)
            DataError.Auth.NOT_AUTHENTICATED -> UiText.StringResource(R.string.error_not_authenticated)
            DataError.Auth.UNKNOWN -> UiText.StringResource(R.string.error_unknown_auth)
            DataError.Auth.TOKEN_REFRESH_FAILED -> UiText.StringResource(R.string.error_token_refresh_failed)
        }
        is DataError.User -> when (this) {
            DataError.User.USER_NOT_FOUND -> UiText.StringResource(R.string.error_user_not_found)
            DataError.User.USER_ALREADY_EXISTS -> UiText.StringResource(R.string.error_user_already_exists)
            DataError.User.PERMISSION_DENIED -> UiText.StringResource(R.string.error_permission_denied)
            DataError.User.UPDATE_FAILED -> UiText.StringResource(R.string.error_update_failed)
        }
        is DataError.Storage -> when (this) {
            DataError.Storage.BUCKET_NOT_FOUND -> UiText.StringResource(R.string.error_bucket_not_found)
            DataError.Storage.QUOTA_EXCEEDED -> UiText.StringResource(R.string.error_quota_exceeded)
            DataError.Storage.UPLOAD_FAILED -> UiText.StringResource(R.string.error_upload_failed)
        }
    }
}
fun Error.asUiText(): UiText {
    return when (this) {
        is DataError.Network -> this.asUiText()
        is DataError.Auth -> this.asUiText()
        is DataError.User -> this.asUiText()
        is DataError.Storage -> this.asUiText()
        is FirstNameError -> this.asUiText()
        is LastNameError -> this.asUiText()
        is BirthDateError -> this.asUiText()
        is GenderError -> this.asUiText()
        is EmailError -> this.asUiText()
        is PhoneNumberError -> this.asUiText()
        is ConfirmedPasswordError -> this.asUiText()
        is PasswordError -> this.asUiText()
       // else -> UiText.StringResource(R.string.error_unknown)
    }
}
