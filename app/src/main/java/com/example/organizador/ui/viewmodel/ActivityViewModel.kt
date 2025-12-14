package com.example.organizador.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.organizador.data.local.entities.ActivityEntity
import com.example.organizador.data.local.entities.CategoryEntity
import com.example.organizador.data.preferences.UserPreferences
import com.example.organizador.data.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val repository: ActivityRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // UI State for filtering
    private val _selectedCategoryFilter = MutableStateFlow<Int?>(null) // null = All
    val selectedCategoryFilter: StateFlow<Int?> = _selectedCategoryFilter
    
    // Status Filter: null = All, false = Pending, true = Completed
    private val _selectedStatusFilter = MutableStateFlow<Boolean?>(false) // Default to Pending
    val selectedStatusFilter: StateFlow<Boolean?> = _selectedStatusFilter
    
    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Combined Flow for Activities
    val activities: StateFlow<List<ActivityEntity>> = combine(
        repository.allActivities,
        _selectedCategoryFilter,
        _selectedStatusFilter,
        _searchQuery
    ) { activities, categoryId, isCompleted, query ->
        activities.filter { activity ->
            // Filter by Category
            val matchCategory = categoryId == null || activity.categoryId == categoryId
            // Filter by Status
            val matchStatus = isCompleted == null || activity.isCompleted == isCompleted
            // Filter by Query
            val matchQuery = activity.title.contains(query, ignoreCase = true)
            
            matchCategory && matchStatus && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preferences
    val themeMode = userPreferences.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
        
    val isConfirmDeleteEnabled = userPreferences.confirmDeleteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultReminderDays = userPreferences.defaultReminderDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val isForegroundServiceEnabled = userPreferences.foregroundServiceEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val isRemindersEnabled = userPreferences.isRemindersEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationType = userPreferences.notificationTypeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "standard")

    val notificationTime = userPreferences.notificationTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8 to 0)

    val onActivityClickAction = userPreferences.onActivityClickActionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "detail")

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Int?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setStatusFilter(isCompleted: Boolean?) {
        _selectedStatusFilter.value = isCompleted
    }

    fun addActivity(activity: ActivityEntity) = viewModelScope.launch {
        repository.insertActivity(activity)
    }

    fun updateActivity(activity: ActivityEntity) = viewModelScope.launch {
        repository.updateActivity(activity)
    }

    fun deleteActivity(activity: ActivityEntity) = viewModelScope.launch {
        repository.deleteActivity(activity)
    }
    
    fun toggleActivityCompletion(activity: ActivityEntity) = viewModelScope.launch {
        repository.updateActivity(activity.copy(isCompleted = !activity.isCompleted))
    }
    
    // Category Actions
    fun addCategory(name: String) = viewModelScope.launch {
        repository.insertCategory(CategoryEntity(name = name))
    }
    
    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    // Preferences Actions
    fun setTheme(theme: String) = viewModelScope.launch {
        userPreferences.setTheme(theme)
    }
    
    fun setConfirmDelete(enabled: Boolean) = viewModelScope.launch {
         userPreferences.setConfirmDelete(enabled)
    }

    fun setDefaultReminderDays(days: Int) = viewModelScope.launch {
        userPreferences.setDefaultReminderDays(days)
    }

    fun setForegroundServiceEnabled(enabled: Boolean) = viewModelScope.launch {
        userPreferences.setForegroundServiceEnabled(enabled)
    }
    
    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        userPreferences.setRemindersEnabled(enabled)
    }

    fun setNotificationType(type: String) = viewModelScope.launch {
        userPreferences.setNotificationType(type)
    }

    fun setNotificationTime(hour: Int, minute: Int) = viewModelScope.launch {
        userPreferences.setNotificationTime(hour, minute)
    }

    fun setOnActivityClickAction(action: String) = viewModelScope.launch {
        userPreferences.setOnActivityClickAction(action)
    }
}

class ActivityViewModelFactory(
    private val repository: ActivityRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
