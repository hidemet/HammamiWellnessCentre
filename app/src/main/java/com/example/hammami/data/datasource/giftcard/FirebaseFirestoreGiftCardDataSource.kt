package com.example.hammami.data.datasource.giftcard

import android.util.Log
import com.example.hammami.domain.model.giftCard.GiftCard
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreGiftCardDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val giftCardsCollection = firestore.collection(FirestoreCollections.GIFT_CARDS)
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    suspend fun storeGiftCard(giftCard: GiftCard) {
        Log.d("GiftCardDataSource", "Creating gift card: $giftCard")

        try {
            val docRef = giftCardsCollection.document()
            val giftCardWithId = giftCard.copy(id = docRef.id)
            docRef.set(giftCardWithId).await()
            Log.d("GiftCardDataSource", "Gift card creata con successo")
        } catch (e: Exception) {
            Log.e("GiftCardDataSource", "Errore nella creazione della gift card", e)
            throw e
        }
    }

    suspend fun createGiftCardWithTransaction(
        giftCard: GiftCard,
        transactionId: String
    ) {
        val docRef = giftCardsCollection.document()
        docRef.set(
            giftCard.copy(
                id = docRef.id,
                transactionId = transactionId
            )
        ).await()
    }

    suspend fun getGiftCardByTransactionId(transactionId: String): GiftCard? =
        giftCardsCollection
            .whereEqualTo(FirestoreFields.TRANSACTION_ID, transactionId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(GiftCard::class.java)

    suspend fun getGiftCardsByUserId(userId: String): List<GiftCard> =
        giftCardsCollection
            .whereEqualTo(FirestoreFields.PURCHASER_ID, userId)
            .get()
            .await()
            .toObjects(GiftCard::class.java)

    suspend fun getGiftCardByCode(code: String): GiftCard? =
        giftCardsCollection
            .whereEqualTo(FirestoreFields.CODE, code)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(GiftCard::class.java)

    suspend fun markGiftCardAsUsed(code: String) {
        val giftCard = getGiftCardByCode(code)
            ?: throw IllegalStateException("Gift card not found")

        giftCardsCollection
            .document(giftCard.id)
            .update(
                mapOf(
                    FirestoreFields.USED to true,
                    FirestoreFields.USED_DATE to Timestamp.now()
                )
            )
            .await()
    }

    suspend fun updateGiftCardUsage(
        giftCardId: String,
        bookingId: String
    ) = executeTransaction { transaction ->
        val giftCardRef = giftCardsCollection.document(giftCardId)

        transaction.update(
            giftCardRef,
            mapOf(
                FirestoreFields.USED to true,
                FirestoreFields.USED_DATE to Timestamp.now(),
                FirestoreFields.USED_IN_BOOKING to bookingId
            )
        )
    }

    suspend fun <T> executeTransaction(operation: (Transaction) -> T): T =
        firestore.runTransaction { transaction ->
            operation(transaction)
        }.await()

    suspend fun batchWriteOperation(operations: (Transaction) -> Unit) {
        executeTransaction { transaction ->
            operations(transaction)
        }
    }

    // Utility functions for references
    fun getGiftCardRef(): DocumentReference =
        giftCardsCollection.document()

    fun getUserRef(userId: String): DocumentReference =
        usersCollection.document(userId)

    object FirestoreFields {
        const val PURCHASER_ID = "purchaserId"
        const val CODE = "code"
        const val USED = "used"
        const val USED_DATE = "usedDate"
        const val USED_IN_BOOKING = "usedInBooking"
        const val VALUE = "value"
        const val EXPIRATION_DATE = "expirationDate"
        const val CREATED_AT = "createdAt"
        const val VALID = "valid"
        const val TRANSACTION_ID = "transactionId"
    }

    object FirestoreCollections {
        const val GIFT_CARDS = "giftCards"
        const val USERS = "users"
    }
}