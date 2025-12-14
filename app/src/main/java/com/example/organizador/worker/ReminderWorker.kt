package com.example.organizador.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.organizador.OrganizadorApplication
import com.example.organizador.metrics.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        val application = applicationContext as OrganizadorApplication
        val repository = application.repository
        val userPreferences = application.userPreferences

        // Get preferences
        val isRemindersEnabled = userPreferences.isRemindersEnabledFlow.first()
        if (!isRemindersEnabled) return Result.success()

        val activities = repository.allActivities.first()
        val currentTime = System.currentTimeMillis()


        
        val notificationType = userPreferences.notificationTypeFlow.first()

        activities.forEach { activity ->
            if (!activity.isCompleted && activity.isReminderEnabled) {
                val dueTime = activity.dueDate
                
                // 1. Extract the Day/Month/Year from the stored DueDate (which is UTC midnight from DatePicker)
                val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = dueTime
                }
                
                // 2. Create Target Calendar in LOCAL time
                val targetCalendar = Calendar.getInstance().apply {
                    // Set Date from UTC calendar
                    set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                    
                    // Set Time from Activity Settings
                    set(Calendar.HOUR_OF_DAY, activity.reminderHour)
                    set(Calendar.MINUTE, activity.reminderMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    
                    // Apply Offset
                    add(Calendar.DAY_OF_YEAR, -activity.reminderOffset)
                }

                val targetTime = targetCalendar.timeInMillis
                
                // We check if "now" is past the target time, but still the same day (to avoid notifying for old stuff infinitely, 
                // though usually we'd want to notify missed ones too. For now: "Same Day" logic + "Time Passed").
                
                if (isSameDay(currentTime, targetTime) && currentTime >= targetTime) {
                     notificationHelper.showNotification(
                        activity.id,
                        "Recordatorio: ${activity.title}",
                        if (activity.reminderOffset == 0) "¡Es para hoy!" else "Faltan ${activity.reminderOffset} días para la entrega.",
                        notificationType
                    )
                }
            }
        }

        return Result.success()
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
