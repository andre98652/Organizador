package com.example.organizador.ui.screens.addedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.organizador.data.local.entities.ActivityEntity
import com.example.organizador.ui.viewmodel.ActivityViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    navController: NavController,
    viewModel: ActivityViewModel,
    activityId: Int?
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableIntStateOf(1) }
    var selectedCategoryName by remember { mutableStateOf("Estudio") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) } 
    
    var reminderEnabled by remember { mutableStateOf(false) }
    var isReminderToday by remember { mutableStateOf(false) }
    var reminderDaysBeforeStr by remember { mutableStateOf("1") }
    
    // Time Picker State
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSystem24Hour = android.text.format.DateFormat.is24HourFormat(context)
    
    var reminderHour by remember { mutableIntStateOf(8) }
    var reminderMinute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = reminderHour, 
        initialMinute = reminderMinute,
        is24Hour = isSystem24Hour
    )
    
    // Date Picker State
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate + 5 * 60 * 60 * 1000) // Offset to ensure UTC day matches roughly
    var showDatePicker by remember { mutableStateOf(false) }

    // Load Data
    val activityList by viewModel.activities.collectAsState()
    
    LaunchedEffect(activityId, activityList) {
        if (activityId != null && activityId != -1) { 
             val activity = activityList.find { it.id == activityId }
             if (activity != null) {
                 title = activity.title
                 description = activity.description
                 dueDate = activity.dueDate
                 reminderEnabled = activity.isReminderEnabled
                 
                 // Reminder Logic
                 if (activity.reminderOffset == 0) {
                     isReminderToday = true
                     reminderDaysBeforeStr = "0"
                 } else {
                     isReminderToday = false
                     reminderDaysBeforeStr = if (activity.reminderOffset > 0) activity.reminderOffset.toString() else "1"
                 }
                 
                 reminderHour = activity.reminderHour
                 reminderMinute = activity.reminderMinute
                 
                 selectedCategoryId = activity.categoryId
                 selectedCategoryName = activity.categoryName
             }
        }
    }
    
    // Categories
    val categories by viewModel.allCategories.collectAsState()
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryId == 0) {
            val first = categories.first()
            selectedCategoryId = first.id
            selectedCategoryName = first.name
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = timePickerState.hour
                    reminderMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimeInput(state = timePickerState) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (activityId == null) "Nueva Actividad" else "Editar Actividad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            val daysBefore = if (isReminderToday) 0 else reminderDaysBeforeStr.toIntOrNull() ?: 1
                            
                            val activity = ActivityEntity(
                                id = activityId ?: 0,
                                title = title,
                                description = description,
                                dueDate = dueDate,
                                reminderOffset = if (reminderEnabled) daysBefore else -1,
                                isReminderEnabled = reminderEnabled,
                                reminderHour = reminderHour,
                                reminderMinute = reminderMinute,
                                categoryId = selectedCategoryId,
                                categoryName = selectedCategoryName,
                                isCompleted = false
                            )
                            if (activityId == null) {
                                viewModel.addActivity(activity)
                            } else {
                                viewModel.updateActivity(activity)
                            }
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Category Selection
            Text("Categoría", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { 
                            selectedCategoryId = category.id
                            selectedCategoryName = category.name
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            // Reminder Section (Moved Up)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recordatorio", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
            }
            
            if (reminderEnabled) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        
                        // "Is Today" Checkbox (Priority Option)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isReminderToday,
                                onCheckedChange = { 
                                    isReminderToday = it
                                    if (it) {
                                        reminderDaysBeforeStr = "0"
                                        // Auto-set Date to "Today" (Current Time) logic is tricky with UTC picker.
                                        // Ideally we set it to UTC Midnight of today to match picker behavior? 
                                        // Or just current millis works if we format in UTC? 
                                        // Let's set it to current millis.
                                        dueDate = System.currentTimeMillis() 
                                    }
                                }
                            )
                            Text("Recordar hoy (Mismo día de entrega)")
                        }
                        
                        HorizontalDivider()
                        
                        // Time Selection
                        Text("Hora del aviso:", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { showTimePicker = true }) {
                            Text(String.format("%02d:%02d", reminderHour, reminderMinute))
                        }

                        // Days Logic (Only if not today)
                        if (!isReminderToday) {
                            OutlinedTextField(
                                value = reminderDaysBeforeStr,
                                onValueChange = { if (it.all { char -> char.isDigit() }) reminderDaysBeforeStr = it },
                                label = { Text("Días antes") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Date Selection (Moved Down)
            Text("Fecha de Entrega", style = MaterialTheme.typography.labelLarge)
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(Modifier.width(16.dp))
                    // Fix: Use UTC TimeZone for formatting to display the selected "Date" correctly (ignoring local offset shift)
                    val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    Text(formatter.format(Date(dueDate)))
                }
            }
        }
    }
}
