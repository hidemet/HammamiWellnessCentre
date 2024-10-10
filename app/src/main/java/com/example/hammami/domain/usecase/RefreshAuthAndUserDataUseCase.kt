package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.model.User
import com.example.hammami.util.PreferencesManager
import javax.inject.Inject

class RefreshAuthAndUserDataUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Result<User, DataError> {
        return if (preferencesManager.isUserLoggedIn()) {
            when (val authResult = authRepository.refreshAuthToken()) {
                is Result.Success -> {
                    val userId = authRepository.getCurrentUserId()
                    userRepository.getUserData(userId)
                }
                is Result.Error -> Result.Error(authResult.error)
            }
        } else {
            Result.Error(DataError.Auth.NOT_AUTHENTICATED)
        }
    }
}