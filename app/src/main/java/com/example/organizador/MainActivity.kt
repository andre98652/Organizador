package com.example.organizador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.organizador.ui.navigation.NavGraph
import com.example.organizador.ui.navigation.Screen
import com.example.organizador.ui.screens.home.HomeScreen
import com.example.organizador.ui.theme.OrganizadorTheme
import com.example.organizador.ui.viewmodel.ActivityViewModel
import com.example.organizador.ui.viewmodel.ActivityViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as OrganizadorApplication
        val viewModel: ActivityViewModel by viewModels {
            ActivityViewModelFactory(app.repository, app.userPreferences)
        }
        
        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = "system")
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            OrganizadorTheme(darkTheme = isDarkTheme) {
                // Foreground Service Logic
                val isForegroundServiceEnabled by viewModel.isForegroundServiceEnabled.collectAsState(initial = false)
                val context = androidx.compose.ui.platform.LocalContext.current
                
                LaunchedEffect(isForegroundServiceEnabled) {
                    val intent = android.content.Intent(context, com.example.organizador.services.ReminderForegroundService::class.java)
                    if (isForegroundServiceEnabled) {
                         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    } else {
                        context.stopService(intent)
                    }
                }

                // Request Notification Permission on Android 13+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                        onResult = { _ -> }
                    )
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val navController = rememberNavController()
                
                // Deep Link Handler
                var startDestination by remember { mutableStateOf(Screen.Home.route) }
                LaunchedEffect(intent) {
                    val activityId = intent.getIntExtra("activity_id", -1)
                    if (activityId != -1) {
                         startDestination = Screen.Detail.createRoute(activityId)
                    }
                }
                
                // Handle new intents (if app is already open)
                DisposableEffect(Unit) {
                    val listener = androidx.core.util.Consumer<android.content.Intent> { newIntent ->
                        val id = newIntent.getIntExtra("activity_id", -1)
                        if (id != -1) {
                            navController.navigate(Screen.Detail.createRoute(id))
                        }
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}