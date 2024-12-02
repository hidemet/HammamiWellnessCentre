package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.NewServicesRepository
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetNewServicesUseCase @Inject constructor(
    private val newServicesRepository: NewServicesRepository
) {
    suspend operator fun invoke(): Result<List<Service>, DataError> = newServicesRepository.getNewServicesData()
}