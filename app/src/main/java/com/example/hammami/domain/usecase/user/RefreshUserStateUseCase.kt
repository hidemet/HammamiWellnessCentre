package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserStateRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.util.PreferencesManager

class RefreshUserStateUseCase @Inject constructor(
    private val userStateRepository: UserStateRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke() {
        if (!preferencesManager.isUserLoggedIn()) {
            userStateRepository.updateUserState(Result.Error(DataError.Auth.NOT_AUTHENTICATED))
            return
        }

        when (val authResult = authRepository.refreshAuthToken()) {
            is Result.Success -> userStateRepository.refreshUserData()
            is Result.Error -> userStateRepository.updateUserState(Result.Error(authResult.error))
        }
    }
}
