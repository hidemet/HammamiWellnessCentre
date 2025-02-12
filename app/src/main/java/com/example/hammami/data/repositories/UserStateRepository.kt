package com.example.hammami.data.repositories

import android.util.Log
import com.example.hammami.data.datasource.user.FirebaseFirestoreUserDataSource
import com.example.hammami.domain.model.User
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserStateRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val authRepository: AuthRepository
) {

    private var listenerRegistration: ListenerRegistration? = null

    fun observeUserChanges(): Flow<Result<User?, DataError>> = callbackFlow {
        val userIdResult = authRepository.getCurrentUserId()
        if (userIdResult is Result.Error) {
            send(Result.Error(userIdResult.error))
            close()
            return@callbackFlow
        }
        val userId = (userIdResult as Result.Success).data

        listenerRegistration?.remove()

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
    fun clearUserListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}