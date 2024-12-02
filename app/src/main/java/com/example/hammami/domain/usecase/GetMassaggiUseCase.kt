package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.MassaggiRepository
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetMassaggiUseCase @Inject constructor(
    private val massaggiRepository: MassaggiRepository
) {
    suspend operator fun invoke(): Result<List<Service>, DataError> = massaggiRepository.getMassaggiData()
}