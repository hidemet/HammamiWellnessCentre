package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.User
import com.example.hammami.core.result.Result
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit, DataError> {
        return userRepository.updateUser(user)
    }
}