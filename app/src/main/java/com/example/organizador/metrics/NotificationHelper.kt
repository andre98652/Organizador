package com.example.organizador.metrics

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.organizador.MainActivity
import com.example.organizador.R
import android.app.TaskStackBuilder
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "activity_reminders"
        const val CHANNEL_NAME = "Recordatorios de Actividad"
        const val CHANNEL_DESCRIPTION = "Notificaciones para actividades próximas"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // Can be configurable
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(activityId: Int, title: String, content: String, type: String = "standard") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Ensure accurate channel exists
        val channelId = when (type) {
            "sound" -> createChannel("reminders_sound", "Recordatorios (Sonido)", NotificationManager.IMPORTANCE_HIGH, sound = true, vibrate = false)
            "vibrate" -> createChannel("reminders_vibrate", "Recordatorios (Vibración)", NotificationManager.IMPORTANCE_HIGH, sound = false, vibrate = true)
            "both" -> createChannel("reminders_both", "Recordatorios (Ambos)", NotificationManager.IMPORTANCE_HIGH, sound = true, vibrate = true)
            else -> createChannel("reminders_standard", "Recordatorios (Estándar)", NotificationManager.IMPORTANCE_HIGH, sound = true, vibrate = false) // Standard usually implies sound
        }

        val resultIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("activity_id", activityId)
        }

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        // Action Intents
        val completeIntent = Intent(context, com.example.organizador.receivers.NotificationReceiver::class.java).apply {
            action = com.example.organizador.receivers.NotificationReceiver.ACTION_COMPLETE
            putExtra(com.example.organizador.receivers.NotificationReceiver.EXTRA_ACTIVITY_ID, activityId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context, 
            activityId, // Unique Request Code
            completeIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, com.example.organizador.receivers.NotificationReceiver::class.java).apply {
            action = com.example.organizador.receivers.NotificationReceiver.ACTION_DISMISS
            putExtra(com.example.organizador.receivers.NotificationReceiver.EXTRA_ACTIVITY_ID, activityId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, 
            activityId + 10000, 
            dismissIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_notification_small, "Completar", completePendingIntent) // Replace icon if needed
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Silenciar", dismissPendingIntent)

        // For older Android versions (Pre-Oreo), configure builder directly
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            when (type) {
                "sound" -> builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
                "vibrate" -> builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                "both" -> builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
        }

        with(NotificationManagerCompat.from(context)) {
            // Use activityId as the notification ID to ensure one notification per task
            notify(activityId, builder.build())
        }
    }
    
    private fun createChannel(channelId: String, name: String, importance: Int, sound: Boolean, vibrate: Boolean): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Check if exists
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, name, importance).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(vibrate)
                    if (!sound) setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
        return channelId
    }
}
