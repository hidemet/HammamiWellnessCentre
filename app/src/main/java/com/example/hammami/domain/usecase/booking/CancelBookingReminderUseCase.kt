package com.example.hammami.domain.usecase.booking

import androidx.work.WorkManager
import javax.inject.Inject

class CancelBookingReminderUseCase @Inject constructor(
    private val workManager: WorkManager
){
    operator fun invoke(bookingId: String) {
        workManager.cancelUniqueWork(bookingId)
    }
}