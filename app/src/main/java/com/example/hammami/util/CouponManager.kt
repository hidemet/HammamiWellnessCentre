package com.example.hammami.util

import com.example.hammami.models.User
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.models.Coupon
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


// Update the generateCouponForUser method in CouponManager
suspend fun generateCouponForUser(value: Int, user: User): Resource<Coupon> {
    return if (user.points.toFloat() >= value / 5) {
        val userId = userProfileRepository.getCurrentUserId()
        if (userId == null){
            return Resource.Error("Utente non autenticato")
        }
        val coupon = generateCoupon(value, userId)
        val updatedUser = deductPoints(user, value * 5)
        userProfileRepository.updateUserProfile(updatedUser)
        userProfileRepository.addCouponToUser(userId, coupon)
        _generatedCoupon.value = coupon.code
        Resource.Success(coupon)
    } else {
        _generatedCoupon.value = null
        Resource.Error("Non hai abbastanza punti")
    }
}

    private fun deductPoints(user: User, pointsToDeduct: Int): User {
        val updatedPoints = (user.points.toFloat() - pointsToDeduct).toInt().toString()
        return user.copy(points = updatedPoints)
    }

    private fun generateCoupon(value: Int, userId: String): Coupon {
        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
        val random = SecureRandom()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        val randomPart = (1..4)
            .map { charPool[random.nextInt(charPool.size)] }
            .joinToString("")
        val code = "${timestamp}${randomPart}${value.toString().padStart(4, '0')}"

        return Coupon(
            id = "",
            userId = userId,
            code = code,
            value = value,
            creationDate = LocalDateTime.now(),
            expirationDate = LocalDateTime.now().plusYears(1),
            isActive = true,
            isRedeemed = false,
            redemptionDate = null
        )
    }

    fun loadCoupons() {
        _couponValues.value = listOf(10, 20, 30) // Example values
    }

    fun resetGeneratedCoupon() {
        _generatedCoupon.value = null
    }
}