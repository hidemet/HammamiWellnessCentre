package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.model.User
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit, DataError> {
        return userRepository.updateUser(user)
    }
}