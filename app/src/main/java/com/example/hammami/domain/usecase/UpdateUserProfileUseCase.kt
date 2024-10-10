package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.model.User
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit, DataError> {
        return userRepository.updateUserProfile(user)
    }
}