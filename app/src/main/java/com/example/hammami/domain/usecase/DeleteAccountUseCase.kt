package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.UserRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        return userRepository.deleteUser()
    }
}