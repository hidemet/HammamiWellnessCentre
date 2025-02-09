package com.example.hammami.data.datasource.user

import android.util.Log
import com.example.hammami.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val usersCollection = firestore.collection("users")

    fun listenToUserDocument(userId: String): Flow<User?> = callbackFlow {
        val listenerRegistration = usersCollection.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Chiude il flow in caso di errore
                return@addSnapshotListener
            }

            val user = snapshot?.toObject(User::class.java)
            trySend(user) // Invia l'utente o null se non esiste
        }
        awaitClose { listenerRegistration.remove() }
    }

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

    suspend fun checkIfAdmin(userId: String): Boolean {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            val isAdmin = userDoc.getBoolean("isadmin") ?: false
            Log.d("FirebaseFirestoreUserDataSource", "checkIfAdmin: User ID $userId, isAdmin: $isAdmin") // Aggiunto log
            isAdmin
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirebaseFirestoreUserDataSource", "checkIfAdmin: Error", e) // Aggiunto log
            throw e
        }
    }

    suspend fun updateUser(uid: String, user: User) {
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

    suspend fun getUserPoints(uid: String): Int {
        return try {
            usersCollection.document(uid).get().await().toObject(User::class.java)?.points ?: 0
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun setUserPoints(uid: String, points: Int) {
        try {
            usersCollection.document(uid).update("points", points).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }


    fun addUserPoints(transaction: Transaction, uid: String, pointsToAdd: Int) {
        val userDocument = usersCollection.document(uid)
        val userSnapshot = transaction.get(userDocument)
        val currentPoints = userSnapshot.getLong("points")?.toInt() ?: 0
        val newPoints = currentPoints + pointsToAdd
        transaction.update(userDocument, "points", newPoints)
        Log.d("UserRepository", "User points updated successfully for user: $uid, new points: $newPoints")
    }


}