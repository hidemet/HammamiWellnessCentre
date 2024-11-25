package com.example.hammami.domain.usecase.auth

import android.util.Log
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        Log.d("SignOutUseCase", "Executing signOut")
        return try {
            authRepository.signOut().also {
                Log.d("SignOutUseCase", "SignOut result: $it")
            }
        } catch (e: Exception) {
            Log.e("SignOutUseCase", "Error during signOut", e)
            Result.Error(DataError.Auth.UNKNOWN)
        }
    }
}