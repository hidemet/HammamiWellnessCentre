package com.example.hammami.domain.usecase

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.ReviewsRepository
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class AddReviewToServiceUseCase @Inject constructor(
    private val reviewRepository: ReviewsRepository
) {
    suspend operator fun invoke(serviceId: String, reviewId: String): Result<Unit, DataError> = reviewRepository.addReviewToService(serviceId, reviewId)
}