package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Result<User, DataError>> = userRepository.getUserData()
}