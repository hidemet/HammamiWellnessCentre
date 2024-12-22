package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.EsteticaRepository
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.User
import javax.inject.Inject

class GetCurrentUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User, DataError> = userRepository.getUserData()

}