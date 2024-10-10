package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit, DataError> {
        return authRepository.signIn(email, password)
    }
}