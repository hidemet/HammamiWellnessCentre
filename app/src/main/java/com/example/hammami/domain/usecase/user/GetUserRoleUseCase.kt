package com.example.hammami.domain.usecase.user

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.data.repositories.UserStateRepository
import com.example.hammami.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserRoleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userStateRepository: UserStateRepository
) {
    operator fun invoke(): Flow<Result<String, DataError>> =
        userStateRepository.observeUserChanges().map { result ->
            when (result) {
                is Result.Success -> {
                    val user = result.data
                    if (user != null) {
                        val role = user.role
                        if (role == "admin") {
                            Result.Success("admin")
                        } else {
                            Result.Success("customer")
                        }
                    } else {
                        Result.Error(DataError.User.USER_NOT_FOUND)
                    }
                }
                is Result.Error -> Result.Error(result.error)
            }
        }
}