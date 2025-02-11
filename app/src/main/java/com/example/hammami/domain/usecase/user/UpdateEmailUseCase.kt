package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.AuthRepository
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError


class UpdateEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newEmail: String): Result<Unit, DataError> = authRepository.updateEmail(newEmail)
}