package com.example.hammami.database

import com.google.firebase.auth.FirebaseAuth

class FirebaseDb {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun createUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
    }

    fun loginUser(email: String, password: String) = firebaseAuth.signInWithEmailAndPassword(email, password)
}