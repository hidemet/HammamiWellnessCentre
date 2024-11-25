package com.example.hammami.domain.usecase.auth

import com.example.hammami.data.repositories.AuthRepository
import javax.inject.Inject

class CheckAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isUserAuthenticated()
    }
}