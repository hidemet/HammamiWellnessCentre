package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import javax.inject.Inject
import com.example.hammami.core.result.Result

class GetUserPointsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Int, DataError> {
        return userRepository.getUserPoints()
    }
}