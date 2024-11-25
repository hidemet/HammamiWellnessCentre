package com.example.hammami.data.repositories

import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.payment.Discount
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class DiscountRepository @Inject constructor(
    private val giftCardRepository: GiftCardRepository,
    private val couponRepository: CouponRepository
)  {
    suspend fun getDiscountByCode(code: String): Result<Discount, DataError> {
        return when {
            code.startsWith("GC") -> getGiftCardDiscount(code)
            code.startsWith("CO") -> getCouponDiscount(code)
            else -> Result.Error(DataError.Payment.INVALID_DISCOUNT_CODE)
        }
    }

    private suspend fun getGiftCardDiscount(code: String): Result<Discount.GiftCardDiscount, DataError> {
        return when (val result = giftCardRepository.getGiftCardByCode(code)) {
            is Result.Success -> {
                val giftCard = result.data
                Result.Success(
                    Discount.GiftCardDiscount(
                        code = giftCard.code,
                        value = giftCard.value,
                        expirationDate = giftCard.expirationDate.toLocalDateTime(),
                        isUsed = giftCard.used
                    )
                )
            }
            is Result.Error -> Result.Error(result.error)
        }
    }

    private suspend fun getCouponDiscount(code: String): Result<Discount.CouponDiscount, DataError> {
        return when (val result = couponRepository.getCouponByCode(code)) {
            is Result.Success -> {
                val coupon = result.data
                Result.Success(
                    Discount.CouponDiscount(
                        code = coupon.code,
                        value = coupon.value,
                        expirationDate = coupon.expirationDate.toLocalDateTime(),
                        isUsed = coupon.isUsed,
                        userId = coupon.userId
                    )
                )
            }
            is Result.Error -> Result.Error(result.error)
        }
    }

    private fun Timestamp.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(toDate().toInstant(), ZoneId.systemDefault())
}