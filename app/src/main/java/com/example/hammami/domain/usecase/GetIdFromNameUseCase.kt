package com.example.hammami.domain.usecase

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.ReviewsRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Review
import javax.inject.Inject

class GetIdFromNameUseCase @Inject constructor(
    private val reviewRepository: ReviewsRepository
) {
    suspend operator fun invoke(nameService: String): Result<String?, DataError> = reviewRepository.getServiceIdFromName(nameService)
}