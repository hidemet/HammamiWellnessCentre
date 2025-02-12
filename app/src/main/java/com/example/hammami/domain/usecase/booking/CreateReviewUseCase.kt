package com.example.hammami.domain.usecase.booking

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.ReviewsRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.Review

import javax.inject.Inject

class CreateReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewsRepository,
) {
    suspend operator fun invoke(
        reviewText: String,
        rating: Float,
        serviceName: String,
        booking: Booking,
        userName: String
    ): Result<Review, DataError> {

        return reviewRepository.createReview(
            reviewText = reviewText,
            rating = rating,
            serviceName = serviceName,
            userName = userName,
            bookingId = booking.id
        )
    }
}
