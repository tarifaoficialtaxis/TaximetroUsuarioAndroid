package com.mitarifamitaxi.taximetrousuario.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.mitarifamitaxi.taximetrousuario.R
import com.mitarifamitaxi.taximetrousuario.activities.taximeter.TaximeterActivity

class NotificationForegroundService : LifecycleService() {

    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            CHANNEL_ID,
            "Ubicación en segundo plano",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificación para seguimiento de ubicación"
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(chan)
    }

    /*private fun buildNotification(): Notification {
        val intent = Intent(this, TaximeterActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taxímetro activo")
            .setContentText("Actualizando tu ubicación en segundo plano")
            .setSmallIcon(R.drawable.logo4)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }*/

    private fun buildNotification(): Notification {
        val intent = Intent(this, TaximeterActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taxímetro activo")
            .setContentText("Actualizando tu ubicación en segundo plano")
            .setSmallIcon(R.drawable.logo4)
            .setContentIntent(pending)
            .setOngoing(true)
            .setAutoCancel(false)

        val notification = builder.build().apply {
            flags = flags or
                    Notification.FLAG_ONGOING_EVENT or
                    Notification.FLAG_NO_CLEAR
        }
        return notification
    }

    override fun onBind(intent: Intent) = super.onBind(intent)
}
