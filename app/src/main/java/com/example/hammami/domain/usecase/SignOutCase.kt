package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        return authRepository.signOut()
    }
}