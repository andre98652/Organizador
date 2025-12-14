package com.example.organizador

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.organizador.data.local.AppDatabase
import com.example.organizador.data.preferences.UserPreferences
import com.example.organizador.data.repository.ActivityRepository
import com.example.organizador.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class OrganizadorApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { ActivityRepository(database.activityDao(), database.categoryDao()) }
    val userPreferences by lazy { UserPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ReminderWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
