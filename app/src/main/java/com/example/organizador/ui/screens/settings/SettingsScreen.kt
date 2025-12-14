package com.example.organizador.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.organizador.ui.viewmodel.ActivityViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ActivityViewModel
) {
    val theme by viewModel.themeMode.collectAsState()
    val confirmDelete by viewModel.isConfirmDeleteEnabled.collectAsState()
    val defaultDays by viewModel.defaultReminderDays.collectAsState()
    val foregroundService by viewModel.isForegroundServiceEnabled.collectAsState()

    // Activity Settings
    val onActivityClickAction by viewModel.onActivityClickAction.collectAsState()
    
    // Notifications State
    val isRemindersEnabled by viewModel.isRemindersEnabled.collectAsState()
    val notificationType by viewModel.notificationType.collectAsState()
    val notificationTime by viewModel.notificationTime.collectAsState() // Pair<Int, Int>

    val categories by viewModel.allCategories.collectAsState()
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    // Time Picker
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = notificationTime.first,
        initialMinute = notificationTime.second
    )
    
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setNotificationTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Agregar Categoría") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                         viewModel.addCategory(newCategoryName)
                         newCategoryName = ""
                         showAddCategoryDialog = false
                    }
                }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                 TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance
            SettingsSection(title = "Apariencia") {
                Column {
                    Text("Tema", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                         FilterChip(
                            selected = theme == "system",
                            onClick = { viewModel.setTheme("system") },
                            label = { Text("Sistema") }
                        )
                         FilterChip(
                            selected = theme == "light",
                            onClick = { viewModel.setTheme("light") },
                            label = { Text("Claro") }
                        )
                         FilterChip(
                            selected = theme == "dark",
                            onClick = { viewModel.setTheme("dark") },
                            label = { Text("Oscuro") }
                        )
                    }
                }
            }

            // Notifications
            SettingsSection(title = "Notificaciones") {
                // Reminders
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))) {
                   Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recordatorios", style = MaterialTheme.typography.titleSmall)
                            Text("Activar notificaciones para tareas próximas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(checked = isRemindersEnabled, onCheckedChange = { viewModel.setRemindersEnabled(it) })
                    }
                }

                // Notification Type
                Text("Tipo de notificación", style = MaterialTheme.typography.bodyMedium)
                CustomDropdown(
                    options = listOf("Estándar", "Con sonido", "Con vibración", "Ambos"),
                    selectedOption = when(notificationType) {
                        "sound" -> "Con sonido"
                        "vibrate" -> "Con vibración"
                        "both" -> "Ambos"
                        else -> "Estándar"
                    },
                    onOptionSelected = { label ->
                        val value = when(label) {
                            "Con sonido" -> "sound"
                            "Con vibración" -> "vibrate"
                            "Ambos" -> "both"
                            else -> "standard"
                        }
                        viewModel.setNotificationType(value)
                    }
                )

                // Default Time
                Text("Hora por defecto", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    val amPm = if (notificationTime.first >= 12) "p. m." else "a. m."
                    val hour12 = if (notificationTime.first % 12 == 0) 12 else notificationTime.first % 12
                    Text(
                         text = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, notificationTime.second, amPm),
                         modifier = Modifier.fillMaxWidth(),
                         textAlign = TextAlign.Start,
                         style = MaterialTheme.typography.bodyLarge,
                         color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Default Days
                Text("Días antes por defecto", style = MaterialTheme.typography.bodyMedium)
                CustomDropdown(
                    options = (1..7).map { it.toString() },
                    selectedOption = defaultDays.toString(),
                    onOptionSelected = { viewModel.setDefaultReminderDays(it.toInt()) }
                )


            }

            // Activities
            SettingsSection(title = "Actividades") {
                SettingsSwitch(
                    title = "Confirmar eliminación",
                    subtitle = "Mostrar confirmación antes de borrar.",
                    checked = confirmDelete,
                    onCheckedChange = { viewModel.setConfirmDelete(it) }
                )
                
                Text("Al tocar una actividad", style = MaterialTheme.typography.bodyMedium)
                CustomDropdown(
                    options = listOf("Abrir detalle", "Editar directamente"),
                    selectedOption = if (onActivityClickAction == "edit") "Editar directamente" else "Abrir detalle",
                    onOptionSelected = { label ->
                        viewModel.setOnActivityClickAction(if (label == "Editar directamente") "edit" else "detail")
                    }
                )
            }
            
            // About
            SettingsSection(title = "Acerca de") {
                Text("Organizador de Actividades v1.0")
                Text("Curso: Computación Móvil", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun CustomDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
             onClick = { expanded = true },
             modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedOption)
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
        
        DropdownMenu( 
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content
    )
}
