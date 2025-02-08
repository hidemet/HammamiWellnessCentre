package com.example.hammami.domain.usecase.user

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.User
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User, DataError> =
        userRepository.getUserById(userId)
}