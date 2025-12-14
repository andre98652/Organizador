package com.example.organizador.data.repository

import com.example.organizador.data.local.dao.ActivityDao
import com.example.organizador.data.local.dao.CategoryDao
import com.example.organizador.data.local.entities.ActivityEntity
import com.example.organizador.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val categoryDao: CategoryDao
) {
    // Activities
    val allActivities: Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    val pendingActivities: Flow<List<ActivityEntity>> = activityDao.getPendingActivities()

    fun getActivitiesByCategory(categoryId: Int): Flow<List<ActivityEntity>> = 
        activityDao.getActivitiesByCategory(categoryId)

    suspend fun getActivityById(id: Int): ActivityEntity? = activityDao.getActivityById(id)

    suspend fun insertActivity(activity: ActivityEntity) = activityDao.insertActivity(activity)

    suspend fun updateActivity(activity: ActivityEntity) = activityDao.updateActivity(activity)

    suspend fun deleteActivity(activity: ActivityEntity) = activityDao.deleteActivity(activity)

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)
}
