package com.example.hammami.util

object Constants {
    const val USER_COLLECTION = "users"

    const val SPLASH_SCREEN_SHARED_PREFERENCES = "SplashScreenSharedPreferences"
    const val SPLASH_SCREEN_KEY = "SplashScreenKey"
    const val USER_SHARED_PREFERENCES = "UserSharedPreferences"
}

object CouponConstants {
    const val MIN_COUPON_VALUE = 10
    const val MAX_COUPON_VALUE = 30
    const val COUPON_VALUE_STEP = 10
    const val MIN_POINTS_REQUIRED = 50
    //const val POINTS_MULTIPLIER = 50

    const val RANDOM_CODE_LENGTH = 4
    const val VALUE_CODE_LENGTH = 4
    const val EXPIRATION_YEARS = 1L
    const val POINTS_MULTIPLIER = 5

    val VALID_COUPON_VALUES = setOf(10, 20, 30)
    val COUPON_CODE_PATTERN = Regex("\\d+[A-Z0-9]{4}\\d{4}")
}

object FirestoreCollections {
    const val COUPONS = "coupons"
    const val USERS = "users"
    const val POINTS_HISTORY = "pointsHistory"
}

object FirestoreFields {
    const val USER_ID = "userId"
    const val CODE = "code"
    const val POINTS = "points"
}