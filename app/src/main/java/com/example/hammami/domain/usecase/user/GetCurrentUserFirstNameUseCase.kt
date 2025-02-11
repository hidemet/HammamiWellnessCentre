package com.example.hammami.domain.usecase.user

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class GetCurrentUserFirstNameUseCase @Inject constructor(
    private val userRepository: UserRepository
){
    suspend operator fun invoke(): Result<String, DataError> {
        return userRepository.getCurrentUserFirstName()
    }
}