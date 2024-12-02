package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.RecommendedRepository
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetRecommendedUseCase @Inject constructor(
    private val recommendedRepository: RecommendedRepository
) {
    suspend operator fun invoke(): Result<List<Service>, DataError> =recommendedRepository.getRecommendedData()
}