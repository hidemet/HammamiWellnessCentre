package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.BenessereRepository
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetBenessereUseCase @Inject constructor(
    private val benessereRepository: BenessereRepository
) {
    suspend operator fun invoke(): Result<List<Service>, DataError> = benessereRepository.getBenessereData()
}