package com.example.organizador.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.organizador.data.local.entities.ActivityEntity
import com.example.organizador.ui.viewmodel.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: ActivityViewModel,
    activityId: Int
) {
    // We need to fetch the activity. 
    // Ideally this is done in ViewModel with a StateFlow for "currentActivity" or similar.
    // For now, let's just collect all activities and find it (inefficient but safe for small data).
    val activities by viewModel.activities.collectAsState()
    val activity = activities.find { it.id == activityId }

    val isConfirmDeleteEnabled by viewModel.isConfirmDeleteEnabled.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar actividad?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (activity != null) {
                            viewModel.deleteActivity(activity)
                            showDeleteDialog = false
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (activity != null) {
                        IconButton(onClick = { 
                            navController.navigate(com.example.organizador.ui.navigation.Screen.AddEdit.createRoute(activity.id))
                        }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                        IconButton(onClick = { 
                            if (isConfirmDeleteEnabled) {
                                showDeleteDialog = true
                            } else {
                                viewModel.deleteActivity(activity)
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (activity == null) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Actividad no encontrada o cargando...")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SuggestionChip(onClick = {}, label = { Text(activity.categoryName) })
                            Spacer(Modifier.width(8.dp))
                            if (activity.isCompleted) {
                                SuggestionChip(
                                    onClick = {}, 
                                    label = { Text("COMPLETADA") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = Color.Green.copy(alpha = 0.2f)
                                    )
                                )
                            } else {
                                SuggestionChip(
                                    onClick = {}, 
                                    label = { Text("PENDIENTE") }
                                )
                            }
                        }
                    }
                }

                // Date & Reminder
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, null)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Fecha de entrega")
                            val dateFormat = remember {
                                SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).apply {
                                    timeZone = TimeZone.getTimeZone("UTC")
                                }
                            }
                            Text(
                                text = dateFormat.format(Date(activity.dueDate)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                if (activity.isReminderEnabled) {
                     OutlinedCard(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, null)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Recordatorio")
                                Text(
                                    text = "${activity.reminderOffset} días antes",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                // Description
                if (activity.description.isNotBlank()) {
                    Text("Descripción", style = MaterialTheme.typography.titleMedium)
                    Text(activity.description, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.weight(1f))

                // Actions
                Button(
                    onClick = { viewModel.toggleActivityCompletion(activity) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (activity.isCompleted) Icons.Outlined.CheckCircle else Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (activity.isCompleted) "Marcar como Pendiente" else "Marcar como Completada")
                }
            }
        }
    }
}
