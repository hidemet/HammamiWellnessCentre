package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.coupon.FirebaseFirestoreCouponDataSource
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import java.security.SecureRandom
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreCouponDataSource,
    private val authRepository: AuthRepository
) {
    suspend fun generateCoupon(value: Double): Result<Coupon, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val requiredPoints = calculateRequiredPoints(value)
                    val userId = uidResult.data

                    val coupon = createCoupon(value, userId)
                    dataSource.createCouponWithPoints(coupon, userId, requiredPoints)

                    Result.Success(coupon)
                } catch (e: Exception) {
                    Result.Error(mapException(e))
                }
            }

            is Result.Error -> Result.Error(uidResult.error)
        }
    }

    suspend fun getUserCoupons(): Result<List<Coupon>, DataError> {
        return when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val coupons = dataSource.getCouponsByUserId(uidResult.data)
                    Result.Success(coupons)
                } catch (e: Exception) {
                    Result.Error(mapException(e))
                }
            }

            is Result.Error -> Result.Error(uidResult.error)
        }
    }

    suspend fun getCouponByCode(code: String): Result<Coupon, DataError> {
        return try {
            val coupon = dataSource.getCouponByCode(code)
                ?: return Result.Error(DataError.Discount.NOT_FOUND)

            Result.Success(coupon)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }


    suspend fun getValidatedCoupon(code: String, amount: Double): Result<Coupon, DataError> {
        return try {
            val coupon = dataSource.getCouponByCode(code)
                ?: return Result.Error(DataError.Coupon.NOT_FOUND)

            when {
                !coupon.isValid() -> Result.Error(DataError.Coupon.INVALID)
                coupon.value > amount -> Result.Error(DataError.Payment.DISCOUNT_EXCEEDS_AMOUNT)
                else -> Result.Success(coupon)
            }
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }


    suspend fun useCoupon(couponCode: String, bookingId: String): Result<Unit, DataError> {
        return try {
            val coupon = dataSource.getCouponByCode(couponCode)
                ?: return Result.Error(DataError.Coupon.NOT_FOUND)

            if (!coupon.isValid()) {
                return Result.Error(DataError.Coupon.INVALID)
            }

            dataSource.updateCouponUsage(coupon.id, bookingId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    private fun createCoupon(value: Double, userId: String) = Coupon(
        code = generateUniqueCode(value),
        value = value,
        userId = userId,
        createdAt = Timestamp.now(),
        expirationDate = calculateExpirationDate()
    )

    private fun calculateRequiredPoints(value: Double): Int = (value * 5).toInt()

    private fun generateUniqueCode(value: Double): String {
        val random = SecureRandom()
        val timestamp = System.currentTimeMillis()
        val chars = ('A'..'Z') + ('0'..'9')
        val randomPart = (1..8).map { chars[random.nextInt(chars.size)] }.joinToString("")
        return "CO${timestamp.toString().takeLast(6)}$randomPart${value.toInt()}"
    }

    private fun calculateExpirationDate(): Timestamp =
        Timestamp(Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))

    private fun mapException(e: Exception): DataError = when (e) {
        is IllegalStateException -> DataError.User.INSUFFICIENT_POINTS
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.User.PERMISSION_DENIED
            FirebaseFirestoreException.Code.NOT_FOUND -> DataError.User.USER_NOT_FOUND
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> DataError.User.COUPON_ALREADY_EXISTS
            FirebaseFirestoreException.Code.CANCELLED -> DataError.Network.OPERATION_CANCELLED
            else -> DataError.Network.UNKNOWN
        }

        is FirebaseNetworkException -> DataError.Network.NO_INTERNET
        else -> DataError.Network.UNKNOWN
    }
}