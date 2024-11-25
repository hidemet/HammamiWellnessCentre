package com.example.hammami.domain.usecase.user

import com.example.hammami.domain.model.User
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class getCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Result<String, DataError> = authRepository.getCurrentUserId()
}