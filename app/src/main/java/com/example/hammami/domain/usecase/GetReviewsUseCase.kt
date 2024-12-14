package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Review
import com.example.hammami.data.repositories.ReviewsRepository
import com.google.firebase.firestore.DocumentReference
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(reviewsPath: List<DocumentReference>?): Result<List<Review>, DataError> = reviewsRepository.getReviewsData(reviewsPath)
}