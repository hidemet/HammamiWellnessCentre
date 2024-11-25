package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.user.FirebaseFirestoreUserDataSource
import com.example.hammami.domain.model.User
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result

import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStateRepository @Inject constructor(
    private val firestoreDataSource: FirebaseFirestoreUserDataSource,
    private val authRepository: AuthRepository
) {
    private val _userData = MutableStateFlow<Result<User?, DataError>>(
        Result.Error(DataError.Auth.NOT_AUTHENTICATED)
    )
    val userData = _userData.asStateFlow()

    suspend fun refreshUserData() {
        when (val uidResult = authRepository.getCurrentUserId()) {
            is Result.Success -> {
                try {
                    val user = firestoreDataSource.fetchUserData(uidResult.data)
                    if (user != null) {
                        _userData.value = Result.Success(user)
                    } else {
                        _userData.value = Result.Error(DataError.User.USER_NOT_FOUND)
                    }
                } catch (e: Exception) {
                    _userData.value = Result.Error(mapExceptionToDataError(e))
                }
            }

            is Result.Error -> {
                _userData.value = Result.Error(uidResult.error)
            }
        }
    }

    internal fun updateUserState(result: Result<User?, DataError>) {
        _userData.value = result
    }


    private fun mapExceptionToDataError(e: Exception): DataError = when (e) {
        is FirebaseFirestoreException -> DataError.User.USER_NOT_FOUND
        else -> DataError.Network.UNKNOWN
    }
}
