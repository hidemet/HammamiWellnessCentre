package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User, DataError> = userRepository.getUserData()
}