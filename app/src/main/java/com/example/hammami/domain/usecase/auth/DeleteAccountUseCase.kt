package com.example.hammami.domain.usecase.auth

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        return userRepository.deleteUser()
    }
}