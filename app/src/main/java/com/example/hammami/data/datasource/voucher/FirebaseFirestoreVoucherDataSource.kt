package com.example.hammami.data.datasource.voucher

import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreVoucherDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val vouchersCollection = firestore.collection("vouchers")

    suspend fun createVoucher(voucher: DiscountVoucher) {
        try {
            val docRef = vouchersCollection.document()
            docRef.set(voucher.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    suspend fun getVoucherByCode(code: String): DiscountVoucher? {
        return try {
            vouchersCollection
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(DiscountVoucher::class.java)
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    suspend fun getUserVouchersByType(userId: String, type: VoucherType): List<DiscountVoucher> {
        return try {
            vouchersCollection
                .whereEqualTo("createdBy", userId)
                .whereEqualTo("type", type)
                .get()
                .await()
                .toObjects(DiscountVoucher::class.java)
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }


    suspend fun getVouchersByUser(userId: String): List<DiscountVoucher> {
        return try {
            vouchersCollection
                .whereEqualTo("createdBy", userId)
                .get()
                .await()
                .toObjects(DiscountVoucher::class.java)
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    suspend fun deleteVoucher(voucherId: String) {
        try {
            vouchersCollection.document(voucherId).delete().await()
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