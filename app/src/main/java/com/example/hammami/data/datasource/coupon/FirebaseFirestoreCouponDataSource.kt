package com.example.hammami.data.datasource.coupon

import com.example.hammami.domain.model.coupon.Coupon

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import com.example.hammami.util.FirestoreCollections
import com.example.hammami.util.FirestoreFields
import com.example.hammami.util.FirestoreFields.POINTS
import com.google.firebase.Timestamp


@Singleton
class FirebaseFirestoreCouponDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val couponsCollection = firestore.collection(FirestoreCollections.COUPONS)
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    private fun DocumentSnapshot.toCoupon(): Coupon {
        return Coupon(
            id = id,
            code = getString("code") ?: "",
            value = getDouble("value") ?: 0.0,
            userId = getString("userId") ?: "",
            createdAt = getTimestamp("createdAt") ?: Timestamp.now(),
            expirationDate = getTimestamp("expirationDate") ?: Timestamp.now(),
            usedInBooking = getString("usedInBooking"),
            isUsed = getBoolean("isUsed") ?: false,
            usedDate = getTimestamp("usedDate")
        )
    }

    private fun Coupon.toFirestoreMap() = mapOf(
        "code" to code,
        "value" to value,
        "userId" to userId,
        "createdAt" to createdAt,
        "expirationDate" to expirationDate,
        "usedInBooking" to usedInBooking,
        "isUsed" to isUsed,
        "usedDate" to usedDate
    )

    suspend fun createCouponDocument(coupon: Coupon) {
        val docRef = couponsCollection.document()
        docRef.set(coupon.copy(id = docRef.id).toFirestoreMap()).await()
    }

    suspend fun getCouponsByUserId(userId: String): List<Coupon> =
        couponsCollection
            .whereEqualTo(FirestoreFields.USER_ID, userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toCoupon() }



    suspend fun getCouponByCode(code: String): Coupon? =
        couponsCollection
            .whereEqualTo(FirestoreFields.CODE, code)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toCoupon()

    suspend fun getUserPoints(userId: String): Long =
        usersCollection
            .document(userId)
            .get()
            .await()
            .getLong(POINTS) ?: 0L

    suspend fun createCouponWithPoints(
        coupon: Coupon,
        userId: String,
        requiredPoints: Int
    ) {
        firestore.runTransaction { transaction ->
            val userRef = usersCollection.document(userId)
            val currentPoints = transaction.get(userRef)
                .getLong(POINTS) ?: 0L

            if (currentPoints < requiredPoints) {
                throw IllegalStateException("Insufficient points")
            }

            val couponRef = couponsCollection.document()
            transaction.set(couponRef, coupon.copy(id = couponRef.id).toFirestoreMap())
            transaction.update(
                userRef,
                POINTS,
                currentPoints - requiredPoints
            )
        }.await()
    }

    suspend fun updateCouponUsage(
        couponId: String,
        bookingId: String
    ) {
        couponsCollection.document(couponId)
            .update(
                mapOf(
                    "isUsed" to true,
                    "usedDate" to Timestamp.now(),
                    "usedInBooking" to bookingId
                )
            )
            .await()
    }

    suspend fun updateUserPoints(
        userId: String,
        pointsDelta: Int
    ) {
        val userRef = usersCollection.document(userId)
        firestore.runTransaction { transaction ->
            val currentPoints = transaction.get(userRef)
                .getLong("points") ?: 0L
            transaction.update(
                userRef,
                "points",
                currentPoints + pointsDelta
            )
        }.await()
    }
}
