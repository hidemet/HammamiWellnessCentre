package com.example.hammami.data.repositories

import androidx.compose.ui.input.key.Key.Companion.D
import androidx.compose.ui.input.key.Key.Companion.F
import com.example.hammami.data.datasource.voucher.FirebaseFirestoreVoucherDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.core.result.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreVoucherDataSource,
    private val userRepository: UserRepository // per gestire i punti
) {
    suspend fun createGiftCard(
        value: Double,
        userId: String,
        transactionId: String
    ): Result<DiscountVoucher, DataError> {
        return try {
            val voucher = DiscountVoucher(
                code = DiscountVoucher.generateCode(value, VoucherType.GIFT_CARD),
                value = value,
                type = VoucherType.GIFT_CARD,
                createdBy = userId,
                creationTransactionId = transactionId,
                expirationDate = calculateExpirationDate()
            )
            dataSource.createVoucher(voucher)
            Result.Success(voucher)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun createCoupon(
        value: Double,
        userId: String,
        requiredPoints: Int
    ): Result<DiscountVoucher, DataError> {
        return try {
            // Verifica e sottrai i punti in una transazione atomica
            when (val pointsResult = userRepository.deductPoints(userId, requiredPoints)) {
                is Result.Success -> {
                    val voucher = DiscountVoucher(
                        code = DiscountVoucher.generateCode(value, VoucherType.COUPON),
                        value = value,
                        type = VoucherType.COUPON,
                        createdBy = userId,
                        expirationDate = calculateExpirationDate()
                    )
                    dataSource.createVoucher(voucher)
                    Result.Success(voucher)
                }

                is Result.Error -> Result.Error(pointsResult.error)
            }
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun useVoucher(code: String, amount: Double): Result<Double, DataError> {
        return try {
            val voucher = dataSource.getVoucherByCode(code)
                ?: return Result.Error(DataError.Voucher.NOT_FOUND)

            when {
                !voucher.isValid() -> Result.Error(DataError.Voucher.EXPIRED)
                voucher.value > amount -> Result.Error(DataError.Voucher.VALUE_EXCEEDS_AMOUNT)
                else -> {
                    dataSource.deleteVoucher(voucher.id)
                    Result.Success(voucher.value)
                }
            }
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserVouchers(userId: String): Result<List<DiscountVoucher>, DataError> {
        return try {
            val vouchers = dataSource.getVouchersByUser(userId)
            Result.Success(vouchers)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    private fun calculateExpirationDate(): Timestamp =
        Timestamp(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)))

    private fun mapExceptionToDataError(e: Exception): DataError = when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> DataError.Voucher.ALREADY_EXISTS
                FirebaseFirestoreException.Code.NOT_FOUND -> DataError.Voucher.NOT_FOUND
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.Voucher.PERMISSION_DENIED
                else -> DataError.Firestore.UNKNOWN
            }

            is FirebaseNetworkException -> DataError.Network.NO_INTERNET
            else -> DataError.Unknown.UNKNOWN
        }
}