package com.example.hammami.data.repositories

import android.util.Log
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


    suspend fun getUserVouchersByType(
        userId: String,
        type: VoucherType
    ): Result<List<Voucher>, DataError> {

        return try {
            Log.d("VoucherRepository", "Fetching vouchers for user: $userId, type: $type")

            val vouchers = dataSource.getUserVouchersByType(userId, type)
            Log.d("VoucherRepository", "Query result: $vouchers")
            Result.Success(vouchers)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
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