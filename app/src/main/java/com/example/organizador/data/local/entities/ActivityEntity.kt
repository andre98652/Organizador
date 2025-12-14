package com.example.organizador.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: Long, // Timestamp
    val reminderOffset: Int, // Days before, 0 = no reminder? OR -1? Let's use 0 for "On day", -1 for none. Or boolean in ViewModel.
    // Actually, requirement says "opción de recordar días antes".
    val isReminderEnabled: Boolean = false,
    val reminderHour: Int = 8, // Default 8 AM
    val reminderMinute: Int = 0, // Default 00
    val categoryName: String, 
    val categoryId: Int, 
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
