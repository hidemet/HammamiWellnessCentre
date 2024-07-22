package com.example.hammami.database

import android.util.Log
import com.example.hammami.models.User
import com.example.hammami.util.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseDb @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    init {
        // Abilita la persistenza offline di Firebase
        FirebaseFirestore.setLoggingEnabled(true)
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(50 * 1024 * 1024) // 50 MB
                        .build()
                )
                .build()
            firestore.firestoreSettings = settings
        } catch (e: Exception) {
            Log.e("FirebaseDb", "Error setting persistence", e)
        }
    }
    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    private val usersCollectionRef = firestore.collection(USER_COLLECTION)

    fun createUser(email: String, password: String) =
        firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun logoutUser() {
        firebaseAuth.signOut()
    }

    fun saveUserInformation(
        userUid: String,
        user: User
    ) = usersCollectionRef.document(userUid).set(user)

    fun loginUser(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun resetPassword(email: String) = firebaseAuth.sendPasswordResetEmail(email)

    fun getCurrentUser() = firebaseAuth.currentUser

    fun isUserSignedIn() = firebaseAuth.currentUser != null

    suspend fun refreshAuthToken(): Boolean = suspendCoroutine { continuation ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }

        user.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(true)
                } else {
                    Log.e("FirebaseDb", "Error refreshing token", task.exception)
                    continuation.resume(false)
                }
            }
    }
}