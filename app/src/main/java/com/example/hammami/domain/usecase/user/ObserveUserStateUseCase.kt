package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserStateRepository
import com.example.hammami.domain.model.User
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveUserStateUseCase @Inject constructor(
    private val userStateRepository: UserStateRepository
) {
    operator fun invoke(): Flow<Result<User?, DataError>> = userStateRepository.userData
}