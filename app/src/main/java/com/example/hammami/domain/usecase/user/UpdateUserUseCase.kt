package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.User
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.AuthRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        updatedUser: User,
        emailChanged: Boolean,
        userPassword: String? = null
    ): Result<Unit, DataError> {

        if (emailChanged) {
            if (userPassword == null) {
                return Result.Error(DataError.Auth.REQUIRED_PASSWORD)
            }
            val reAuthResult = authRepository.reauthenticateUser(userPassword)
            if (reAuthResult is Result.Error) {
                return reAuthResult
            }
        }
        return userRepository.updateUser(updatedUser)
    }
}