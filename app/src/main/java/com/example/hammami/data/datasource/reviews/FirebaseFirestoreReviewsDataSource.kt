package com.example.hammami.data.datasource.reviews

import com.example.hammami.domain.model.Review
import com.example.hammami.domain.model.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreReviewsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val reviewsCollection = firestore.collection("/Recensioni")

    suspend fun fetchReviewsData(reviewsPathList: List<DocumentReference>?): List<Review> {
        val allReviews = mutableListOf<Review>()
        try {
            reviewsPathList?.forEach { reviewPath ->
                val review =
                    firestore.document(reviewPath.path).get().await().toObject(Review::class.java)
                review?.let { allReviews.add(it) }
            }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
        return allReviews
    }

 fun addReviewData(transaction: Transaction, review: Review): String {
    return try {
        val document = transaction.run {
            val newDocRef = reviewsCollection.document()
            set(newDocRef, review)
            newDocRef
        }
        document.id
    } catch (e: FirebaseFirestoreException) {
        throw e
    }
}

}