package com.example.hammami.domain.usecase.booking


import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.hammami.presentation.notification.NotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SendCancellationNotificationUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val getBookingByIdUseCase: GetBookingByIdUseCase, //Dipendenza
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(bookingId: String) {  //Ora è una suspend fun
        // Crea i dati da passare al worker
        val data = Data.Builder()
            .putString(NotificationWorker.KEY_BOOKING_ID, bookingId)
            .putBoolean(NotificationWorker.KEY_IS_CANCELLATION, true) // Flag per indicare la cancellazione
            .build()

        // Crea la richiesta di lavoro (OneTimeWorkRequest)
        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .build()  // Nessun ritardo, esecuzione immediata

        // Accoda la richiesta
        workManager.enqueue(notificationWork)
        Log.d(
            "BookingDetailViewModel",
            "Notification sent for cancelled booking: $bookingId"
        )
    }
}