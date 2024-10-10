package com.example.hammami.data.datasource.auth

import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun createUser(email: String, password: String): FirebaseUser? {
        return try {
            if (email.isBlank() || password.isBlank()) {
                Log.e("FirebaseAuthDataSource", "Tentativo di creazione utente con email o password vuota")
                throw IllegalArgumentException("Email e password non possono essere vuoti")
            }

            Log.d("FirebaseAuthDataSource", "Tentativo di creazione utente con email: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Log.d("FirebaseAuthDataSource", "Utente creato con successo: ${result.user?.uid}")
            result.user
        } catch (e: FirebaseAuthException) {
            Log.e("FirebaseAuthDataSource", "Errore nella creazione dell'utente", e)
            throw e
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        return try {
            Log.d("FirebaseAuthDataSource", "Tentativo di login con email: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("FirebaseAuthDataSource", "Login effettuato con successo: ${result.user?.uid}")
            result.user
        } catch (e: FirebaseAuthException) {
            Log.e("FirebaseAuthDataSource", "Errore nel login", e)
            throw e
        }
    }

    fun signOut() {
        Log.d("FirebaseAuthDataSource", "Esecuzione logout")
        auth.signOut()
    }

    suspend fun resetPassword(email: String) {
        try {
            Log.d("FirebaseAuthDataSource", "Invio email di reset password a: $email")
            auth.sendPasswordResetEmail(email).await()
            Log.d("FirebaseAuthDataSource", "Email di reset password inviata con successo")
        } catch (e: FirebaseAuthException) {
            Log.e("FirebaseAuthDataSource", "Errore nell'invio dell'email di reset password", e)
            throw e
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun reauthenticateUser(currentPassword: String) {
        val user = getCurrentUser()
        if (user != null) {
            try {
                Log.d("FirebaseAuthDataSource", "Tentativo di riautenticazione per l'utente: ${user.uid}")
                val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
                user.reauthenticate(credential).await()
                Log.d("FirebaseAuthDataSource", "Riautenticazione completata con successo")
            } catch (e: FirebaseAuthException) {
                Log.e("FirebaseAuthDataSource", "Errore durante la riautenticazione", e)
                throw e
            }
        } else {
            Log.e("FirebaseAuthDataSource", "Tentativo di riautenticazione fallito: nessun utente corrente")
            throw FirebaseAuthException("", "Nessun utente corrente")
        }
    }

    suspend fun updateEmail(email: String) {
        val user = getCurrentUser()
        if (user != null) {
            try {
                Log.d("FirebaseAuthDataSource", "Tentativo di aggiornamento email per l'utente: ${user.uid}")
                user.verifyBeforeUpdateEmail(email).await()
                Log.d("FirebaseAuthDataSource", "Email aggiornata con successo")
            } catch (e: FirebaseAuthException) {
                Log.e("FirebaseAuthDataSource", "Errore nell'aggiornamento dell'email", e)
                throw e
            }
        } else {
            Log.e("FirebaseAuthDataSource", "Tentativo di aggiornamento email fallito: nessun utente corrente")
            throw FirebaseAuthException("", "Nessun utente corrente")
        }
    }

    suspend fun deleteUserAuth() {
        val user = getCurrentUser()
        if (user != null) {
            try {
                Log.d("FirebaseAuthDataSource", "Tentativo di eliminazione account per l'utente: ${user.uid}")
                user.delete().await()
                Log.d("FirebaseAuthDataSource", "Account eliminato con successo")
            } catch (e: FirebaseAuthException) {
                Log.e("FirebaseAuthDataSource", "Errore nell'eliminazione dell'account", e)
                throw e
            }
        } else {
            Log.e("FirebaseAuthDataSource", "Tentativo di eliminazione account fallito: nessun utente corrente")
            throw FirebaseAuthException("", "Nessun utente corrente")
        }
    }
}