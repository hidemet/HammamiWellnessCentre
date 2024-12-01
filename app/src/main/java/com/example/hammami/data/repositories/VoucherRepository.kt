package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.voucher.FirebaseFirestoreVoucherDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.core.result.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreVoucherDataSource,
    private val authRepository: AuthRepository // per gestire l'utente corrente
) {

    suspend fun saveVoucher(
        voucher: Voucher
    ): Result<Voucher, DataError> {
        return try {
            dataSource.saveVoucher(voucher)
            Result.Success(voucher)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }



    suspend fun getUserVouchersByType(type: VoucherType): Result<List<Voucher>, DataError> {
        return when (val userIdResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val vouchers = dataSource.getUserVouchersByType(userIdResult.data, type)
                    Result.Success(vouchers)
                } catch (e: Exception) {
                    Result.Error(mapExceptionToDataError(e))
                }
            }

            is Result.Error -> Result.Error(userIdResult.error)
        }
    }

    suspend fun getVoucherByTransactionId(transactionId: String): Result<Voucher, DataError> {
        return try {
            val voucher = dataSource.getVoucherByTransactionId(transactionId)
                ?: return Result.Error(DataError.Voucher.NOT_FOUND)

            Result.Success(voucher)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getVoucherByCode(code: String): Result<Voucher, DataError> {
        return try {
            val voucher = dataSource.getVoucherByCode(code)
                ?: return Result.Error(DataError.Voucher.NOT_FOUND)

            Result.Success(voucher)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    suspend fun getUserVouchers(): Result<List<Voucher>, DataError> {
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

    suspend fun deleteVoucher(code: String): Result<Unit, DataError> {
        return try {
            dataSource.deleteVoucher(code)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }


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