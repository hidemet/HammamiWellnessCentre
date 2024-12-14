package com.example.hammami.domain.usecase

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.ReviewsRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Review
import javax.inject.Inject

class SetReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewsRepository
) {
    suspend operator fun invoke(review: Review): Result<Pair<String, List<Review>>, DataError> = reviewRepository.addReviewData(review)
}