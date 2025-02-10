package com.example.hammami.domain.usecase.booking

import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.hammami.domain.model.Booking
import com.example.hammami.presentation.notification.NotificationWorker
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleBookingReminderUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    operator fun invoke(booking: Booking) {
        val data = Data.Builder()
            .putString(NotificationWorker.KEY_BOOKING_ID, booking.id)
            .build()

        // Calcola il ritardo (normalmente 24 ore prima)
        val delayInMillis = booking.startDate.toDate().time - System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        // PER I TEST: imposta il ritardo a 1 minuto DOPO l'istante corrente.
        // val delayInMillis = 60 * 1000 // 1 minuto in millisecondi

        // Impedisci la programmazione di notifiche nel passato.
        if (delayInMillis <= 0) {
            Log.w(
                "ScheduleReminder",
                "Impossibile programmare la notifica, data passata. Booking ID: ${booking.id}"
            )
            return // Non programmare notifiche per date passate
        }

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(Duration.ofMillis(delayInMillis))  // Usa Duration
            .setInputData(data)
            .build()

        workManager.enqueue(notificationWork)
        Log.d(
            "ScheduleReminder",
            "Notification scheduled for bookingId: ${booking.id}, delay: $delayInMillis ms"
        )
    }
}