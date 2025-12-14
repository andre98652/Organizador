package com.example.organizador.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val THEME = stringPreferencesKey("theme")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val DEFAULT_REMINDER_DAYS = intPreferencesKey("default_reminder_days")
        val FOREGROUND_SERVICE_ENABLED = booleanPreferencesKey("foreground_service_enabled")
        
        // New Keys
        val IS_REMINDERS_ENABLED = booleanPreferencesKey("is_reminders_enabled")
        val NOTIFICATION_TYPE = stringPreferencesKey("notification_type") // "standard", "sound", "vibrate", "both"
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        
        val ON_ACTIVITY_CLICK_ACTION = stringPreferencesKey("on_activity_click_action") // "detail", "edit"
    }

    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME] ?: "system" }

    val confirmDeleteFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[CONFIRM_DELETE] ?: true }
        
    val defaultReminderDaysFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[DEFAULT_REMINDER_DAYS] ?: 1 }
        
    val foregroundServiceEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[FOREGROUND_SERVICE_ENABLED] ?: true }
        
    val isRemindersEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_REMINDERS_ENABLED] ?: true }

    val notificationTypeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATION_TYPE] ?: "standard" }

    val notificationTimeFlow: Flow<Pair<Int, Int>> = context.dataStore.data
        .map { preferences -> 
            val hour = preferences[NOTIFICATION_HOUR] ?: 8
            val minute = preferences[NOTIFICATION_MINUTE] ?: 0
            hour to minute
        }

    val onActivityClickActionFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[ON_ACTIVITY_CLICK_ACTION] ?: "detail" }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences -> preferences[THEME] = theme }
    }

    suspend fun setConfirmDelete(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CONFIRM_DELETE] = enabled
        }
    }
    
    suspend fun setDefaultReminderDays(days: Int) {
        context.dataStore.edit { preferences ->
             preferences[DEFAULT_REMINDER_DAYS] = days
        }
    }
    
    suspend fun setForegroundServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FOREGROUND_SERVICE_ENABLED] = enabled
        }
    }
    
    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[IS_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setNotificationType(type: String) {
        context.dataStore.edit { preferences -> preferences[NOTIFICATION_TYPE] = type }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences -> 
            preferences[NOTIFICATION_HOUR] = hour 
            preferences[NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setOnActivityClickAction(action: String) {
        context.dataStore.edit { preferences -> preferences[ON_ACTIVITY_CLICK_ACTION] = action }
    }
}
