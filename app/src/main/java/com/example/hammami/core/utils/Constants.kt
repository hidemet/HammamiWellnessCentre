package com.example.hammami.core.utils

object Constants {
    const val USER_COLLECTION = "users"

    const val SPLASH_SCREEN_SHARED_PREFERENCES = "SplashScreenSharedPreferences"
    const val SPLASH_SCREEN_KEY = "SplashScreenKey"
    const val USER_SHARED_PREFERENCES = "UserSharedPreferences"
}

object CouponConstants {
    const val MIN_COUPON_VALUE = 10.0
    const val MAX_COUPON_VALUE = 30.0
    const val COUPON_VALUE_STEP = 10.0
    const val MIN_POINTS_REQUIRED = 50

    const val CODE_LENGTH = 10
    const val EXPIRATION_DAYS = 365L
    const val POINTS_MULTIPLIER = 5

    val VALID_COUPON_VALUES = setOf(10, 20, 30)
    val COUPON_CODE_PATTERN = Regex("^CO\\d{6}[A-Z0-9]{8}\$")
}

enum class FirestoreCollections {
    COUPONS, USERS, POINTS_HISTORY, TREATMENTS, STAFF, BOOKINGS
}


object FirestoreFields {
    const val USER_ID = "userId"
    const val CODE = "code"
    const val POINTS = "points"
    const val VALUE = "value"
    const val CREATED_AT = "createdAt"
    const val EXPIRATION_DATE = "expirationDate"
    const val IS_USED = "isUsed"
    const val USED_DATE = "usedDate"
    const val USED_IN_TRANSACTION = "usedInTransaction"



}