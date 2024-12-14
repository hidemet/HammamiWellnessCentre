package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserStateRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.User
import kotlinx.coroutines.flow.Flow


class ObserveUserChangesUseCase @Inject constructor(
    private val userStateRepository: UserStateRepository
) {
    operator fun invoke(): Flow<Result<User?, DataError>> =
        userStateRepository.observeUserChanges()
}