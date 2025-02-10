package com.example.hammami.data.repositories

import android.util.Log
import androidx.core.app.PendingIntentCompat.send
import com.example.hammami.data.datasource.user.FirebaseFirestoreUserDataSource
import com.example.hammami.domain.model.User
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserStateRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val authRepository: AuthRepository
) {

    /*
        fun observeUserChanges(): Flow<Result<User?, DataError>> = flow {
            val userIdResult = authRepository.getCurrentUserId()
            if (userIdResult is Result.Error) {
                emit(Result.Error(userIdResult.error))
                return@flow
            }
            val userId = (userIdResult as Result.Success).data

            firestoreDataSource.listenToUserDocument(userId).collect { user ->  // Usa collect, non collectLatest
                if (user != null) {
                    emit(Result.Success(user))
                } else {
                    emit(Result.Error(DataError.User.USER_NOT_FOUND))
                }
            }
        }
    */
    private var listenerRegistration: ListenerRegistration? = null

    fun observeUserChanges(): Flow<Result<User?, DataError>> = callbackFlow {
        val userIdResult = authRepository.getCurrentUserId()
        if (userIdResult is Result.Error) {
            send(Result.Error(userIdResult.error))
            close()
            return@callbackFlow
        }
        val userId = (userIdResult as Result.Success).data

        listenerRegistration?.remove() // Rimuovi il listener precedente

        // Usa collect per consumare il flow.
        firestoreDataSource.listenToUserDocument(userId).collect { user ->
            if (user != null) {
                trySend(Result.Success(user))
            } else {
                trySend(Result.Error(DataError.User.USER_NOT_FOUND))
            }
        }
        awaitClose {  //Chiude il listener
            Log.d("UserStateRepository", "Cleaning up Firestore listener")
            listenerRegistration?.remove()
        }
    }
    // Metodo per cancellare esplicitamente il listener
    fun clearUserListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
//    // Se vuoi mantenere un flusso, puoi farlo, ma assicurati di poterlo cancellare:
//    private var userChangesFlow: Flow<Result<User?, DataError>>? = null
//    private var listenerRegistration: ListenerRegistration? = null
//
//    fun observeUserChanges(): Flow<Result<User?, DataError>> = flow {
//        val userIdResult = authRepository.getCurrentUserId()
//        if (userIdResult is Result.Error) {
//            emit(Result.Error(userIdResult.error))
//            return@flow  // Importante: esci dal flow se non c'è un utente
//        }
//        val userId = (userIdResult as Result.Success).data
//
//        //IMPORTANTE: Se c'è già un listener, rimuovilo prima di crearne uno nuovo
//        listenerRegistration?.remove()
//
//        listenerRegistration = firestoreDataSource.listenToUserDocument(userId).collect{user ->
//            if (user != null) {
//                emit(Result.Success(user))
//            } else {
//                emit(Result.Error(DataError.User.USER_NOT_FOUND))
//            }
//        }
//    }
//
//    // Metodo per cancellare esplicitamente il listener
//    fun clearUserListener() {
//        listenerRegistration?.remove()
//        listenerRegistration = null
//        userChangesFlow = null // Resetta anche il flow
//    }
//}