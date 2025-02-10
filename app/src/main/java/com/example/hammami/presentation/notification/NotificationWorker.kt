package com.example.hammami.presentation.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.usecase.booking.GetBookingByIdUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.app.ActivityCompat

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getBookingByIdUseCase: GetBookingByIdUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val bookingId = inputData.getString(KEY_BOOKING_ID) ?: return@withContext Result.failure()
        val isCancellation = inputData.getBoolean(KEY_IS_CANCELLATION, false) // Recupera il flag

        when (val bookingResult = getBookingByIdUseCase(bookingId)) {
            is com.example.hammami.core.result.Result.Success -> {
                val booking = bookingResult.data

                val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                if (isCancellation) {
                    notificationBuilder
                        .setContentTitle(context.getString(R.string.notification_cancellation_title))
                        .setContentText(context.getString(R.string.notification_cancellation_text, booking.serviceName))

                } else {
                    // Notifica standard (promemoria)
                    notificationBuilder
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_text, booking.serviceName, DateTimeUtils.formatDate(booking.startDate)))
                }


                val notificationManager = NotificationManagerCompat.from(context)
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.w("NotificationWorker", "Missing notification permissions.")
                        return@withContext Result.failure() // Fallimento per mancanza permessi
                    }
                    notificationManager.notify(bookingId.hashCode(), notificationBuilder.build())
                    Result.success()
                } catch (e: SecurityException) {
                    Log.e("NotificationWorker", "Security error: ${e.message}", e)
                    Result.failure() // Fallimento
                }
            }
            is com.example.hammami.core.result.Result.Error -> {
                Log.e("NotificationWorker", "Failed to get booking details: ${bookingResult.error.asUiText().asString(context)}")
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_BOOKING_ID = "booking_id"
        const val KEY_IS_CANCELLATION = "is_cancellation" // Chiave per il flag di cancellazione
        const val CHANNEL_ID = "booking_reminder_channel"
        const val CHANNEL_NAME = "Booking Reminders"
    }
}