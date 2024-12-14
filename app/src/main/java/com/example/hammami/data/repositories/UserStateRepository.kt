package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.user.FirebaseFirestoreUserDataSource
import com.example.hammami.domain.model.User
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result

import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStateRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val authRepository: AuthRepository
) {

    fun observeUserChanges(): Flow<Result<User?, DataError>> = flow {
        val userIdResult = authRepository.getCurrentUserId()
        if (userIdResult is Result.Error) {
            emit(Result.Error(userIdResult.error))
            return@flow
        }
        val userId = (userIdResult as Result.Success).data

        firestoreDataSource.listenToUserDocument(userId).collect { user ->
            if (user != null) {
                emit(Result.Success(user))
            } else {
                emit(Result.Error(DataError.User.USER_NOT_FOUND))
            }
        }
    }

}
