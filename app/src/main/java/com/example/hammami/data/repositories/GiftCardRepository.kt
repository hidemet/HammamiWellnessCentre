package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.data.datasource.giftcard.FirebaseFirestoreGiftCardDataSource
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftCardRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreGiftCardDataSource,
    private val authRepository: AuthRepository
) {

    suspend fun validateAndMarkGiftCardAsUsed(code: String): Result<Double, DataError> {
        return try {
            val giftCard = dataSource.getGiftCardByCode(code)
                ?: return Result.Error(DataError.User.GIFT_CARD_NOT_FOUND)

            if (!giftCard.isValid()) {
                return Result.Error(DataError.User.GIFT_CARD_NOT_VALID)
            }

            dataSource.markGiftCardAsUsed(code)
            Result.Success(giftCard.value)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun getGiftCardByTransactionId(transactionId: String): Result<GiftCard, DataError> {
        return try {
            val giftCard = dataSource.getGiftCardByTransactionId(transactionId)
                ?: return Result.Error(DataError.GiftCard.NOT_FOUND)
            Result.Success(giftCard)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun getGiftCardsByUser(userId: String): Result<List<GiftCard>, DataError> {
        return try {
            val giftCards = dataSource.getGiftCardsByUserId(userId)
            Result.Success(giftCards)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun createGiftCard(
        value: Double,
        purchaserId: String,
        transactionId: String,
        recipientEmail: String? = null
    ): Result<GiftCard, DataError> {
        return try {
            val giftCard = GiftCard(
                code = generateUniqueCode(value),
                value = value,
                purchaserId = purchaserId,
                transactionId = transactionId,
                expirationDate = calculateExpirationDate(),
                createdAt = Timestamp.now()
            )
            dataSource.storeGiftCard(giftCard)
            Result.Success(giftCard)
        } catch (e: Exception) {
            Log.e("GiftCardRepository", "Error creating gift card", e)
            Result.Error(mapException(e))
        }
    }

    suspend fun getValidatedGiftCard(code: String, amount: Double): Result<GiftCard, DataError> {
        return try {
            val giftCard = dataSource.getGiftCardByCode(code)
                ?: return Result.Error(DataError.User.GIFT_CARD_NOT_FOUND)

            when {
                !giftCard.isValid() -> Result.Error(DataError.User.GIFT_CARD_NOT_VALID)
                giftCard.value > amount -> Result.Error(DataError.Payment.DISCOUNT_EXCEEDS_AMOUNT)
                else -> Result.Success(giftCard)
            }
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun getGiftCardByCode(code: String): Result<GiftCard, DataError> {
        return try {
            val giftCard = dataSource.getGiftCardByCode(code)
                ?: return Result.Error(DataError.Discount.NOT_FOUND)

            Result.Success(giftCard)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }




    private fun generateUniqueCode(value: Double): String {
        val charPool = ('A'..'Z') + ('0'..'9')
        val random = SecureRandom()
        val timestamp = System.currentTimeMillis()
        val randomPart = (1..8)
            .map { charPool[random.nextInt(charPool.size)] }
            .joinToString("")

        return buildString {
            append("GC") // Prefisso Gift Card
            append(timestamp.toString().takeLast(6))
            append(randomPart)
            append(value.toString().padStart(3, '0'))
        }
    }


    private fun calculateExpirationDate(): Timestamp =
        Timestamp(
            Date.from(
                LocalDateTime.now()
                    .plusYears(1)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
        )

    private fun mapException(e: Exception): DataError = when (e) {
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.NOT_FOUND -> DataError.GiftCard.NOT_FOUND
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.User.PERMISSION_DENIED
            else -> DataError.Network.SERVER_ERROR
        }

        is FirebaseNetworkException -> DataError.Network.NO_INTERNET
        else -> DataError.Network.UNKNOWN
    }
}

