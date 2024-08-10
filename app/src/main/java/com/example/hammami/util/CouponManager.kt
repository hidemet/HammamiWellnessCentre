// CouponManager.kt
package com.example.hammami.util

import com.example.hammami.models.User
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CouponManager @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    private val _couponValues = MutableStateFlow<List<Int>>(emptyList())
    val couponValues: StateFlow<List<Int>> get() = _couponValues

    private val _generatedCoupon = MutableStateFlow<String?>(null)
    val generatedCoupon: StateFlow<String?> get() = _generatedCoupon

    suspend fun generateCouponForUser(value: Int, user: User): Resource<String> {
        return if (user.points.toFloat() >= value / 5) {
            val couponCode = generateCoupon(value)
            val updatedUser = deductPoints(user, value * 5)
            userProfileRepository.updateUserProfile(updatedUser)
            _generatedCoupon.value = couponCode
            Resource.Success(couponCode)
        } else {
            _generatedCoupon.value = null
            Resource.Error("Insufficient points")
        }
    }

    private fun deductPoints(user: User, pointsToDeduct: Int): User {
        val updatedPoints = (user.points.toFloat() - pointsToDeduct).toInt().toString()
        return user.copy(points = updatedPoints)
    }

    private fun generateCoupon(value: Int): String {
        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
        val random = SecureRandom()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        val randomPart = (1..4)
            .map { charPool[random.nextInt(charPool.size)] }
            .joinToString("")

        return "${timestamp}${randomPart}${value.toString().padStart(4, '0')}"
    }

    fun loadCoupons() {
        _couponValues.value = listOf(10, 20, 30) // Example values
    }

    fun resetGeneratedCoupon() {
        _generatedCoupon.value = null
    }
}