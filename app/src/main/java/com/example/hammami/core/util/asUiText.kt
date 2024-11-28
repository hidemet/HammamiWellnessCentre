package com.example.hammami.core.util

import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.error.Error
import com.example.hammami.domain.error.ValidationError
import com.example.hammami.domain.error.ValidationError.*
import com.example.hammami.domain.error.ValidationError.User.*
import com.example.hammami.domain.error.ValidationError.Card.*
import com.example.hammami.domain.error.ValidationError.DiscountError.*

fun FirstNameError.asUiText(): UiText {
    return when (this) {
        FirstNameError.EMPTY -> UiText.StringResource(R.string.error_first_name_empty)
        FirstNameError.TOO_SHORT -> UiText.StringResource(R.string.error_first_name_too_short)
    }
}

fun LastNameError.asUiText(): UiText {
    return UiText.StringResource(R.string.error_last_name_empty)
}

fun DiscountError.asUiText(): UiText {
    return when (this) {
        INVALID_DISCOUNT -> UiText.StringResource(R.string.error_invalid_discount)
        EXCEEDS_AMOUNT -> UiText.StringResource(R.string.discount_exceeds_amount)
        EMPTY_CODE -> TODO()
        NOT_FOUND -> TODO()
    }
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
            DataError.Network.OPERATION_CANCELLED -> UiText.StringResource(R.string.error_operation_cancelled)
            DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(R.string.error_service_unavailable)
        }

        is DataError.Firestore -> when (this) {
            DataError.Firestore.UNKNOWN -> UiText.StringResource(R.string.error_unknown_firestore)
        }

        is DataError.Unknown -> when (this) {
            DataError.Unknown.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
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
            DataError.User.INVALID_INPUT -> UiText.StringResource(R.string.error_invalid_input)
            DataError.User.INSUFFICIENT_POINTS -> UiText.StringResource(R.string.insufficient_points)
            DataError.User.COUPON_NOT_FOUND -> UiText.StringResource(R.string.error_coupon_not_found)
            DataError.User.COUPON_ALREADY_EXISTS -> UiText.StringResource(R.string.error_coupon_already_exists)
            DataError.User.COUPON_EXPIRED -> UiText.StringResource(R.string.error_coupon_expired)
            DataError.User.COUPON_ALREADY_USED -> UiText.StringResource(R.string.error_coupon_already_used)
            DataError.User.GIFT_CARD_NOT_FOUND -> UiText.StringResource(R.string.error_gift_card_not_found)
            DataError.User.GIFT_CARD_NOT_VALID -> UiText.StringResource(R.string.error_gift_card_not_valid)
            DataError.User.UNKNOWN -> UiText.StringResource(R.string.error_unknown_user)
        }

        is DataError.Storage -> when (this) {
            DataError.Storage.BUCKET_NOT_FOUND -> UiText.StringResource(R.string.error_bucket_not_found)
            DataError.Storage.QUOTA_EXCEEDED -> UiText.StringResource(R.string.error_quota_exceeded)
            DataError.Storage.UPLOAD_FAILED -> UiText.StringResource(R.string.error_upload_failed)
        }

        is DataError.Voucher -> when (this) {
            DataError.Voucher.NOT_FOUND -> UiText.StringResource(R.string.error_voucher_not_found)
            DataError.Voucher.ALREADY_EXISTS -> UiText.StringResource(R.string.error_voucher_already_exists)
            DataError.Voucher.VALUE_EXCEEDS_AMOUNT -> UiText.StringResource(R.string.error_voucher_exceeds_amount)
            DataError.Voucher.PERMISSION_DENIED -> UiText.StringResource(R.string.error_voucher_permission_denied)
            DataError.Voucher.INSUFFICIENT_POINTS -> UiText.StringResource(R.string.error_insufficient_points)
        }

        is DataError.Payment -> when (this) {
            DataError.Payment.INVALID_DISCOUNT_CODE -> UiText.StringResource(R.string.error_invalid_discount_code)
            DataError.Payment.INVALID_AMOUNT -> UiText.StringResource(R.string.error_invalid_amount)
            DataError.Payment.UNKNOWN -> UiText.StringResource(R.string.error_unknown_payment)
            DataError.Payment.DISCOUNT_EXCEEDS_AMOUNT -> UiText.StringResource(R.string.error_discount_exceeds_amount)
            DataError.Payment.INVALID_PAYMENT_INFO -> UiText.StringResource(R.string.error_invalid_payment_info)
        }
    }
}

fun ExpiryDateError.asUiText(): UiText {
    return when (this) {
        ExpiryDateError.EMPTY -> UiText.StringResource(R.string.error_expiry_date_empty)
        ExpiryDateError.INVALID_FORMAT -> UiText.StringResource(R.string.error_expiry_date_invalid_format)
        ExpiryDateError.EXPIRED -> UiText.StringResource(R.string.error_expiry_date_expired)
    }
}

fun NumberError.asUiText(): UiText {
    return when (this) {
        NumberError.EMPTY -> UiText.StringResource(R.string.error_card_number_empty)
        NumberError.INVALID_FORMAT -> UiText.StringResource(R.string.error_card_number_invalid_format)
        NumberError.INVALID_CARD -> UiText.StringResource(R.string.error_card_number_invalid_card)
        NumberError.INVALID_LENGTH -> UiText.StringResource(R.string.error_card_number_invalid_length)
    }
}

fun CvvError.asUiText(): UiText {
    return when (this) {
        CvvError.EMPTY -> UiText.StringResource(R.string.error_cvv_empty)
        CvvError.INVALID_FORMAT -> UiText.StringResource(R.string.error_cvv_invalid_format)
        CvvError.INVALID_LENGTH -> UiText.StringResource(R.string.error_cvv_invalid_length)
    }
}

fun Error.asUiText(): UiText {
    return when (this) {
        is DataError.Network -> this.asUiText()
        is DataError.Auth -> this.asUiText()
        is DataError.User -> this.asUiText()
        is DataError.Storage -> this.asUiText()
        is DataError.Payment -> this.asUiText()
        is DataError.Firestore -> this.asUiText()
        is DataError.Unknown -> this.asUiText()
        is DataError.Voucher -> this.asUiText()
        is FirstNameError -> this.asUiText()
        is LastNameError -> this.asUiText()
        is BirthDateError -> this.asUiText()
        is GenderError -> this.asUiText()
        is EmailError -> this.asUiText()
        is PhoneNumberError -> this.asUiText()
        is ConfirmedPasswordError -> this.asUiText()
        is PasswordError -> this.asUiText()
        is ExpiryDateError -> this.asUiText()
        is NumberError -> this.asUiText()
        is CvvError -> this.asUiText()
        is DiscountError -> this.asUiText()
        is Payment -> this.asUiText()
    }
}
