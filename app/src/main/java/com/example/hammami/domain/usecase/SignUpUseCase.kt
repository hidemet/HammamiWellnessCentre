package com.example.hammami.domain.usecase

import android.util.Log
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.model.User
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String, userData: User): Result<User, DataError> {
        return try {
            Log.d("SignUpUseCase", "Attempting user registration")
            val result = userRepository.signUp(email, password, userData)
            Log.d("SignUpUseCase", "Registration result: $result")
            result
        } catch (e: Exception) {
            Log.e("SignUpUseCase", "Unexpected error during registration", e)
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}