package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.BestDealsRepository
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetBestDealsUseCase @Inject constructor(
    private val bestDealsRepository: BestDealsRepository
) {
    suspend operator fun invoke(): Result<List<Service>, DataError> = bestDealsRepository.getBestDealsData()
}