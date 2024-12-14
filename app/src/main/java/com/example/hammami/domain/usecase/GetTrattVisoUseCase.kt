package com.example.hammami.domain.usecase

import com.example.hammami.domain.model.Service
import com.example.hammami.data.repositories.EsteticaRepository
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetTrattVisoUseCase @Inject constructor(
    private val esteticaRepository: EsteticaRepository
) {
    suspend operator fun invoke(): Result<List<Service>, DataError> = esteticaRepository.getTrattVisoData()

}