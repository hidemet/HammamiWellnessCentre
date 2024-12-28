package com.example.hammami.domain.usecase.appointment

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.AppointmentRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Booking
import javax.inject.Inject

class GetNewAppointmentUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(clientName: String): Result<List<Booking>, DataError> = appointmentRepository.getNewAppointmentData(clientName)

}