package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class DeductUserPointsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, points: Int): Result<Unit, DataError> =
        userRepository.deductPoints(userId, points)
}
