package com.example.hammami.domain.usecase.auth

import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit, DataError> {
        return authRepository.resetPassword(email)
    }
}