package com.example.organizador.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.organizador.OrganizadorApplication
import com.example.organizador.metrics.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_COMPLETE = "com.example.organizador.ACTION_COMPLETE"
        const val ACTION_DISMISS = "com.example.organizador.ACTION_DISMISS"
        const val EXTRA_ACTIVITY_ID = "activity_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getIntExtra(EXTRA_ACTIVITY_ID, -1)
        if (activityId == -1) return

        val app = context.applicationContext as OrganizadorApplication
        val repository = app.repository
        
        // Cancel notification immediately to give feedback
        NotificationManagerCompat.from(context).cancel(activityId)

        when (intent.action) {
            ACTION_COMPLETE -> {
                scope.launch {
                    val activity = repository.getActivityById(activityId)
                    if (activity != null) {
                        repository.updateActivity(activity.copy(isCompleted = true))
                    }
                }
            }
            ACTION_DISMISS -> {
                // Already cancelled above
            }
        }
    }
}
