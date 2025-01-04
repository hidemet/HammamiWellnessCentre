package com.example.hammami.presentation.ui.features

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiverFragment : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "appointment_channel"
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getStringExtra("appointmentId")

        Log.d("NotificationTest", "Notification received for appointmentId: $appointmentId")
        createNotificationChannel(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NotificationTest", "Permission not granted")
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Promemoria Appuntamento")
            .setContentText("Hai un appuntamento domani!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(appointmentId.hashCode(), builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Appointment Reminders"
            val descriptionText = "Channel for appointment reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}