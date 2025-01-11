package com.example.hammami.data.datasource.voucher

import android.util.Log
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




     fun createVoucherDocument(transaction: Transaction, voucher: Voucher) : String {
        return try {
            val documentReference = vouchersCollection.document()
            transaction.set(documentReference, voucher)
             val voucherId = documentReference.id
            voucherId
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

    suspend fun getVoucherById(id:String): Voucher? {
        return try {
            vouchersCollection
                .document(id)
                .get()
                .await()
                .toObject(Voucher::class.java)
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

     suspend fun deleteVoucher(transaction: Transaction, code: String) {
        try {
            // 1. Cerca il documento con il codice fornito (ora asincrono e al di fuori della transazione)
            val querySnapshot = vouchersCollection.whereEqualTo("code", code).limit(1).get().await()

            // 2. Verifica se è stato trovato un documento
            if (querySnapshot.isEmpty) {
                throw FirebaseFirestoreException("Voucher not found with code: $code", FirebaseFirestoreException.Code.NOT_FOUND)
            }

            // 3. Ottieni il riferimento al documento
            val voucherDocumentRef = querySnapshot.documents[0].reference

            // 4. Elimina il documento all'interno della transazione
            transaction.delete(voucherDocumentRef)

            Log.d("FirestoreVoucherDataSource", "Voucher with code: $code deleted successfully in transaction.")

        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreVoucherDataSource", "Error deleting voucher with code: $code", e)
            throw e // Rilancia l'eccezione per la gestione a livello superiore
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