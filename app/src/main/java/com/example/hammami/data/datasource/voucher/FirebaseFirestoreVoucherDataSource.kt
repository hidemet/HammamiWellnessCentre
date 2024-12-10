package com.example.hammami.data.datasource.voucher

import android.util.Log
import com.example.hammami.domain.model.User
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import java.lang.Exception

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreVoucherDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val vouchersCollection = firestore.collection("vouchers")
    private val usersCollection = firestore.collection("users")




     fun createVoucherDocument(transaction: Transaction, voucher: Voucher) {
        try {
            val voucherDocument = firestore.collection("vouchers").document()
            transaction.set(voucherDocument, voucher)
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    suspend fun saveVoucher(voucher: Voucher) {
        try {
            val docRef = vouchersCollection.document()
            docRef.set(voucher).await()
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

//    suspend fun executeVoucherRedemption(
//        userId: String,
//        requiredPoints: Int,
//        voucher: Voucher
//    ) {
//        firestore.runTransaction { transaction ->
//            val userDoc = usersCollection.document(userId)
//            val userData = transaction.get(userDoc).toObject(User::class.java)
//                ?: throw FirebaseFirestoreException("User not found", FirebaseFirestoreException.Code.NOT_FOUND)
//
//            if (userData.points < requiredPoints) {
//                throw FirebaseFirestoreException(
//                    "Insufficient points",
//                    FirebaseFirestoreException.Code.FAILED_PRECONDITION
//                )
//            }
//
//            transaction.update(userDoc, "points", userData.points - requiredPoints)
//
//            val voucherDoc = vouchersCollection.document()
//            transaction.set(voucherDoc, voucher)
//        }.await()
//    }

    suspend fun getVoucherByCode(code: String): Voucher? {
        return try {
            vouchersCollection
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(Voucher::class.java)
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    suspend fun getUserVouchersByType(userId: String, type: VoucherType): List<Voucher> {
        return try {
            val snapshot = vouchersCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .get()
                .await()

            Log.d("FirestoreVoucherDataSource", "Query snapshot size: ${snapshot.size()}")
            Log.d("FirestoreVoucherDataSource", "Documents: ${snapshot.documents.map { it.data }}")


            snapshot.documents.mapNotNull { it.toObject(Voucher::class.java) }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherDataSource", "Error fetching vouchers", e)
            throw mapFirebaseException(e)
        }
    }

    suspend fun getVoucherByTransactionId(transactionId: String): Voucher? {
        return try {
            vouchersCollection
                .whereEqualTo("creationTransactionId", transactionId)
                .limit(1)
                .get()
                .await()
                .documents.firstOrNull()
                ?.toObject(Voucher::class.java)
        } catch (e: Exception) {
            Log.e("VoucherDataSource", "Error getting voucher: ", e)
            throw mapFirebaseException(e)
        }
    }

    suspend fun deleteVoucher(code: String) {
        try {
            val snapshot = vouchersCollection
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
            doc?.reference?.delete()?.await()

        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    private fun mapFirebaseException(e: Exception): Throwable {
        when (e) {
            is FirebaseNetworkException -> throw e
            is FirebaseFirestoreException -> throw e
            else -> throw e
        }
    }

}