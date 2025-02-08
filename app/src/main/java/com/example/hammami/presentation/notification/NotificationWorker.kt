package com.example.hammami.presentation.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getBookingByIdUseCase: GetBookingByIdUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result { // Usa il tipo corretto
        return withContext(Dispatchers.IO) {
            val bookingId = inputData.getString(KEY_BOOKING_ID) ?: return@withContext Result.failure()
            Log.d("NotificationWorker", "Notifica con bookingId: $bookingId")

            when (val bookingResult = getBookingByIdUseCase(bookingId)) {
                is com.example.hammami.core.result.Result.Success -> {
                    val booking = bookingResult.data

                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_calendar)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(
                            context.getString(
                                R.string.notification_text,
                                booking.serviceName,
                                DateTimeUtils.formatDate(booking.startDate)
                            )
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

                    val notificationManager = NotificationManagerCompat.from(context)
                    try {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.w("NotificationWorker", "Mancano i permessi per le notifiche.")
                            return@withContext Result.failure() // Fallimento per mancanza permessi
                        }

                        notificationManager.notify(bookingId.hashCode(), notification)
                        Log.d("NotificationWorker", "Notifica mostrata per bookingId $bookingId")
                        Result.success() // Successo!
                    } catch (e: SecurityException) {
                        Log.e("NotificationWorker", "Errore di sicurezza: ${e.message}", e)
                        Result.failure() // Fallimento
                    }
                }
                is com.example.hammami.core.result.Result.Error -> {
                    Log.e("NotificationWorker", "Impossibile ottenere dettagli prenotazione: ${bookingResult.error.asUiText().asString(context)}")
                    Result.failure() // Fallimento
                }
            }
        }
    }
    companion object {
        const val KEY_BOOKING_ID = "booking_id"
        const val CHANNEL_ID = "booking_reminder_channel"
        const val CHANNEL_NAME = "Booking Reminders"
    }
}