package com.example.organizador.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.organizador.data.local.entities.ActivityEntity
import com.example.organizador.data.local.entities.CategoryEntity
import com.example.organizador.ui.navigation.Screen
import com.example.organizador.ui.viewmodel.ActivityViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ActivityViewModel
) {
    val activities by viewModel.activities.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    
    // Preferences
    val confirmDelete by viewModel.isConfirmDeleteEnabled.collectAsState()
    val onActivityClickAction by viewModel.onActivityClickAction.collectAsState()

    // Search
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Confirmation Dialog State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<ActivityEntity?>(null) }

    if (showDeleteDialog && activityToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false 
                activityToDelete = null
            },
            title = { Text("¿Eliminar actividad?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        activityToDelete?.let { viewModel.deleteActivity(it) }
                        showDeleteDialog = false
                        activityToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        activityToDelete = null
                    }
                ) {
                   Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {

            TopAppBar(
                title = { 
                    Text(
                        "Pendientes", 
                        style = MaterialTheme.typography.titleMedium, // Smaller font for compact look
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Blend with background
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.height(56.dp), // Force standard "Dense" height (default M3 is 64dp)
                // windowInsets = WindowInsets(0.dp), // Optional: if status bar overlap is handled elsewhere
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = { 
                                    viewModel.setStatusFilter(null)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (selectedStatus == null) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Pendientes") },
                                onClick = { 
                                    viewModel.setStatusFilter(false)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (selectedStatus == false) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Completadas") },
                                onClick = { 
                                    viewModel.setStatusFilter(true)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (selectedStatus == true) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(64.dp),
                windowInsets = WindowInsets(0.dp)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pend.") },
                    label = { Text("Pend.") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
                    label = { Text("Config") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEdit.createRoute()) }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar tarea...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Category Chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("Todas Categorias") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category.id,
                        onClick = { viewModel.setCategoryFilter(if (selectedCategory == category.id) null else category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            // Activity List
            if (activities.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).alpha(0.5f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Todo al día!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Relájate o agrega una nueva actividad.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activities, key = { it.id }) { activity ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    if (confirmDelete) {
                                        activityToDelete = activity
                                        showDeleteDialog = true
                                        false
                                    } else {
                                        viewModel.deleteActivity(activity)
                                        true
                                    }
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color,
                                            androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                        )
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Borrar",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            },
                            content = {
                                ActivityItem(
                                    activity = activity,
                                    onItemClick = { 
                                        if (onActivityClickAction == "edit") {
                                            navController.navigate(Screen.AddEdit.createRoute(activity.id))
                                        } else {
                                            navController.navigate(Screen.Detail.createRoute(activity.id)) 
                                        }
                                    },
                                    onDeleteClick = { 
                                        if (confirmDelete) {
                                            activityToDelete = activity
                                            showDeleteDialog = true
                                        } else {
                                            viewModel.deleteActivity(activity)
                                        }
                                    },
                                    onToggleComplete = { viewModel.toggleActivityCompletion(activity) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItem(
    activity: ActivityEntity,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    // Date Formatter (UTC to avoid shifts)
    val dateFormat = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }
    
    // Time Formatter
    val timeFormat = remember {
        java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    // Calculations
    // Calculations
    val context = androidx.compose.ui.platform.LocalContext.current
    val daysRemaining = remember(activity.dueDate) {
        // 1. Get current local date components
        val localCal = java.util.Calendar.getInstance()
        val year = localCal.get(java.util.Calendar.YEAR)
        val month = localCal.get(java.util.Calendar.MONTH)
        val day = localCal.get(java.util.Calendar.DAY_OF_MONTH)

        // 2. Create UTC timestamp for this local date (normalized to midnight UTC)
        val todayUtcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        todayUtcCal.clear()
        todayUtcCal.set(year, month, day)
        val todayUtcMillis = todayUtcCal.timeInMillis

        // 3. Compare with activity.dueDate (which is already UTC midnight)
        val diff = activity.dueDate - todayUtcMillis
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    
    val isToday = daysRemaining == 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow // Better contrast against background
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = dateFormat.format(Date(activity.dueDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chips Row (FlowRow would be better but simple Row with scroll or wrapping if needed. 
            // Mockup shows multiple rows of chips or one wrapped row. Let's use FlowRow if available or multiple Rows.
            // Since FlowRow is experimental in some versions, I'll use a wrapper or just a Scrollable Row for safety or fixed layout)
            // Let's us a LazyRow for chips to be safe on small screens
            
            // Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // 1. Due Date Chip
                val (dueLabel, dueColor, dueTextColor) = when {
                     // Overdue or Today -> RED
                    isToday || daysRemaining < 0 -> Triple(
                        if (isToday) "Vence hoy" else "Vencido hace ${-daysRemaining} días",
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer
                    )
                     // < 3 Days -> ORANGE
                    daysRemaining < 3 -> Triple(
                        if (daysRemaining == 1) "Vence mañana" else "Vence en $daysRemaining días",
                        MaterialTheme.colorScheme.tertiaryContainer, 
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                     // Safe -> GREEN/PRIMARY
                    else -> Triple(
                        "Vence en $daysRemaining días",
                        MaterialTheme.colorScheme.surfaceContainerHigh, 
                        MaterialTheme.colorScheme.onSurface
                    )
                }
                
                AssistChip(
                    onClick = {},
                    label = { Text(dueLabel, color = dueTextColor) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = dueColor),
                    border = null,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50) // Pill shape
                )
                
                // 2. Reminder Chip
                if (activity.isReminderEnabled) {
                    val reminderLabel = if (activity.reminderOffset == 0) "Recordar hoy" else "Recordar ${activity.reminderOffset} días"
                    AssistChip(
                        onClick = {},
                        label = { Text(reminderLabel, color = MaterialTheme.colorScheme.onSecondaryContainer) },
                         colors = AssistChipDefaults.assistChipColors(
                             containerColor = MaterialTheme.colorScheme.secondaryContainer
                         ),
                        border = null,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
                    
                    // Time Chip
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, activity.reminderHour)
                        set(java.util.Calendar.MINUTE, activity.reminderMinute)
                    }
                    val timeStr = timeFormat.format(cal.time)
                    
                    AssistChip(
                        onClick = {},
                        label = { Text(timeStr, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                         colors = AssistChipDefaults.assistChipColors(
                             containerColor = MaterialTheme.colorScheme.primaryContainer
                         ),
                        border = null,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
                }
            }    


            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer: Chips Left, Delete Right
            Row(
                 modifier = Modifier.fillMaxWidth(),
                 verticalAlignment = Alignment.CenterVertically
            ) {
                 // Group Chips
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(activity.categoryName) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF2d4e44).copy(alpha = 0.5f),
                            labelColor = Color(0xFF80cbc4)
                        ),
                        border = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val status = if (activity.isCompleted) "COMPLETADO" else "PENDIENTE"
                    AssistChip(
                        onClick = {},
                        label = { Text(status) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = AssistChipDefaults.assistChipBorder(enabled = true)
                    )
                 }

                Spacer(modifier = Modifier.weight(1f))

                // Toggle Complete Button
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .background(
                            color = if(activity.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha=0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Check, 
                        contentDescription = "Complete",
                        tint = if (activity.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), 
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


