package com.example.hammami.database

import com.example.hammami.model.User
import com.example.hammami.util.Constants.Companion.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDb @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
){

    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    private val usersCollectionRef = Firebase.firestore.collection(USER_COLLECTION)

    fun createUser(email: String, password: String) =
        firebaseAuth.createUserWithEmailAndPassword(email, password)


    fun saveUserInformation(
        userUid: String,
        user: User
    ) = usersCollectionRef.document(userUid).set(user)

    fun loginUser(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun resetPassword(email: String) = firebaseAuth.sendPasswordResetEmail(email)
}