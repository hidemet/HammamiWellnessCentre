package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.voucher.FirebaseFirestoreVoucherDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.core.result.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreVoucherDataSource,
    private val userRepository: UserRepository // per gestire i punti
    private val authRepository: AuthRepository // per gestire l'utente corrente
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

    suspend fun createVoucher(
        voucher: DiscountVoucher
    ): Result<DiscountVoucher, DataError> {
        return try {
            dataSource.createVoucher(voucher)
            Result.Success(voucher)
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

    suspend fun getUserVouchers(): Result<List<DiscountVoucher>, DataError> {
        return try {
            when (val userIdResult = authRepository.getCurrentUserId()) {
                is Result.Success -> {
                    val userId = userIdResult.data
                    val vouchers = dataSource.getVouchersByUser(userId)
                    Result.Success(vouchers)
                }

                is Result.Error -> Result.Error(userIdResult.error)
            }
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