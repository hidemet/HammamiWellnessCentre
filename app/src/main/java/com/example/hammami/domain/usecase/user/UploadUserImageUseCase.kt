package com.example.hammami.domain.usecase.user

import android.net.Uri
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import javax.inject.Inject

class UploadUserImageUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imageUri: Uri): Result<String, DataError> = userRepository.uploadUserImage(imageUri)
}