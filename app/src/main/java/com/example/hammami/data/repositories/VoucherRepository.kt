package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.data.datasource.voucher.FirebaseFirestoreVoucherDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.core.result.Result
import com.example.hammami.domain.factory.VoucherFactory
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreVoucherDataSource,
    private val voucherFactory: VoucherFactory,
    private val userRepository: UserRepository, // per gestire i punti dell'utente
    private val authRepository: AuthRepository, // per gestire l'utente corrente,
    private val firestore: FirebaseFirestore
) {


    suspend fun redeemVoucher(
        userId: String,
        requiredPoints: Int,
        value: Double,
        type: VoucherType
    ): Result<Voucher, DataError> {
        return try {
            val newVoucher = voucherFactory.createVoucher(userId, value, type)

            firestore.runTransaction { transaction ->
                // 1. Decrementa i punti dell'utente
                val deductPointsResult =
                    userRepository.deductPoints(transaction, userId, requiredPoints)
                if (deductPointsResult is Result.Error) {
                    throw Exception("Deduct points failed: ${deductPointsResult.error}")
                }

                // 2. Crea il documento del voucher
                val createVoucherResult = createVoucherDocument(transaction, newVoucher)
                if (createVoucherResult is Result.Error) {
                    throw Exception("Create voucher failed: ${createVoucherResult.error}")
                }

                newVoucher // Restituisco il voucher creato
            }.await()
            Result.Success(newVoucher)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun deleteVoucher(transaction: Transaction, code: String): Result<Unit, DataError> {
        return try {
            val voucherDoc = firestore.collection("vouchers").document(code)
            transaction.delete(voucherDoc)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun createVoucherDocument(
        transaction: Transaction,
        voucher: Voucher
    ): Result<String, DataError> {
        return try {
            val voucherId = dataSource.createVoucherDocument(transaction, voucher)
            Result.Success(voucherId)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }


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


    suspend fun getVoucherById(id: String): Result<Voucher, DataError> {
        return try {
            val voucher = dataSource.getVoucherById(id) ?: return Result.Error(DataError.Voucher.NOT_FOUND)
            Result.Success(voucher)
        } catch (e: Exception) {
            Result.Error(mapExceptionToDataError(e))
        }
    }
}