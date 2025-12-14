package com.example.organizador.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.organizador.MainActivity
import com.example.organizador.OrganizadorApplication
import com.example.organizador.R
import com.example.organizador.metrics.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRunning = false

    companion object {
        const val NOTIFICATION_ID = 999
        const val CHANNEL_ID = "foreground_service_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForegroundService()
            startMonitoring()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Organizador Activo")
            .setContentText("Monitoreando tus actividades pendientes...")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, R.drawable.ic_app_logo))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            
        // For Android 14+ specific type requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoring() {
        serviceScope.launch {
            val app = application as OrganizadorApplication
            val repository = app.repository
            val notificationHelper = NotificationHelper(applicationContext)

            while (isActive) {
                // Logic similar to Worker but running in loop
                val activities = repository.allActivities.first()
                val currentTime = System.currentTimeMillis()


                
                val notificationType = app.userPreferences.notificationTypeFlow.first()

                activities.forEach { activity ->
                    if (!activity.isCompleted && activity.isReminderEnabled) {
                        val dueTime = activity.dueDate
                        
                        // 1. Extract the Day/Month/Year from the stored DueDate (UTC)
                        val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = dueTime
                        }

                        // 2. Create Target Calendar in LOCAL time
                        val targetCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                            
                            set(Calendar.HOUR_OF_DAY, activity.reminderHour)
                            set(Calendar.MINUTE, activity.reminderMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            
                            add(Calendar.DAY_OF_YEAR, -activity.reminderOffset)
                        }

                        val targetTime = targetCalendar.timeInMillis
                        
                        if (isSameDay(currentTime, targetTime) && currentTime >= targetTime) {
                             notificationHelper.showNotification(
                                activity.id,
                                "Recordatorio (Live): ${activity.title}",
                                if (activity.reminderOffset == 0) "¡Es para hoy!" else "Faltan ${activity.reminderOffset} días para la entrega.",
                                notificationType
                            )
                        }
                    }
                }

                delay(60 * 1000L) // Check every minute
            }
        }
    }
    
    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Estado del Servicio",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
    }
}
