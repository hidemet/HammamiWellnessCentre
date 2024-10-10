package com.example.hammami.domain.usecase

import android.net.Uri
import com.example.hammami.data.repositories.UserRepository
import javax.inject.Inject

class UploadUserImageUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imageUri: Uri): Result<String, DataError> = userRepository.uploadProfileImage(imageUri)
}