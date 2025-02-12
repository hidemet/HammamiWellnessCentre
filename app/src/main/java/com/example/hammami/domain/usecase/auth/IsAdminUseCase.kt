package com.example.hammami.domain.usecase.auth

import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject


class IsAdminUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<Boolean, DataError> {
        return when (val userIdResult = authRepository.getCurrentUserId()) {
            is Result.Success ->
                when(val isAdminResult = userRepository.isUserAdmin(userIdResult.data))
                {
                    is Result.Success -> Result.Success(isAdminResult.data)
                    is Result.Error -> Result.Error(isAdminResult.error)
                }
            is Result.Error -> Result.Error(userIdResult.error)
        }
    }
}