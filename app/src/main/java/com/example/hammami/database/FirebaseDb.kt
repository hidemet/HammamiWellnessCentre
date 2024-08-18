package com.example.hammami.database

import android.net.Uri
import com.example.hammami.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDb @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await().user!!

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun saveUserInformation(userUid: String, user: User) =
        usersCollection.document(userUid).set(user).await()

    suspend fun loginUser(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).await().user!!

    suspend fun resetPassword(email: String) =
        auth.sendPasswordResetEmail(email).await()

    fun getCurrentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): User =
        usersCollection.document(uid).get().await().toObject(User::class.java)
            ?: throw Exception("User not found")

    suspend fun getCurrentUserProfile(): User {
        val uid = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
        return getUserProfile(uid)
    }

    suspend fun updateUserProfile(user: User) {
        val uid = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).set(user).await()
    }

    suspend fun uploadProfileImage(imageUri: Uri): String {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_images/$filename")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteUserProfile() {
        val uid = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).delete().await()
        getCurrentUser()?.delete()?.await()
    }
}