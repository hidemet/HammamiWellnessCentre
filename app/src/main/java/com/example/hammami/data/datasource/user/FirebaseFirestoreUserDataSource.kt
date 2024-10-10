package com.example.hammami.data.datasource.user

import com.example.hammami.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val usersCollection = firestore.collection("users")

suspend fun saveUserInformation(userUid: String, user: User) {
        try {
            usersCollection.document(userUid).set(user).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchUserData(uid: String): User? {
        try {
            return usersCollection.document(uid).get().await().toObject(User::class.java)
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun updateUserProfile(uid: String, user: User) {
        try {
            usersCollection.document(uid).set(user).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun deleteUserProfile(uid: String) {
        try {
            usersCollection.document(uid).delete().await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
}