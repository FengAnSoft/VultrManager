package com.example.vultrmanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vultrmanager.VultrManagerApp
import com.example.vultrmanager.ui.account.AccountScreen
import com.example.vultrmanager.ui.instances.CreateInstanceScreen
import com.example.vultrmanager.ui.instances.InstanceDetailScreen
import com.example.vultrmanager.ui.instances.InstanceListScreen
import com.example.vultrmanager.ui.settings.SettingsScreen
import com.example.vultrmanager.ui.theme.VultrManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as VultrManagerApp
        val themeStore = app.appContainer.themeStoreRef

        setContent {
            val darkMode by themeStore.darkMode.collectAsStateWithLifecycle()
            VultrManagerTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination =
                        if (app.appContainer.apiKeyStoreRef.hasApiKey()) "list" else "setup"

                    NavHost(navController = navController, startDestination = startDestination) {

                        composable("list") {
                            InstanceListScreen(
                                repository = app.appContainer.repository,
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenAccount = { navController.navigate("account") },
                                onInstanceClick = { id -> navController.navigate("detail/$id") },
                                onCreateInstance = { navController.navigate("create") }
                            )
                        }

                        composable(
                            route = "detail/{instanceId}",
                            arguments = listOf(navArgument("instanceId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val instanceId = backStackEntry.arguments?.getString("instanceId").orEmpty()
                            InstanceDetailScreen(
                                repository = app.appContainer.repository,
                                instanceId = instanceId,
                                onBack = { navController.popBackStack() },
                                onDeleted = { navController.navigate("list") { popUpTo("list") { inclusive = true } } }
                            )
                        }

                        composable("account") {
                            AccountScreen(
                                repository = app.appContainer.repository,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                apiKeyStore = app.appContainer.apiKeyStoreRef,
                                repository = app.appContainer.repository,
                                themeStore = app.appContainer.themeStoreRef,
                                requireSetup = false,
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.navigate("list") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }
                            )
                        }

                        composable("setup") {
                            SettingsScreen(
                                apiKeyStore = app.appContainer.apiKeyStoreRef,
                                repository = app.appContainer.repository,
                                themeStore = app.appContainer.themeStoreRef,
                                requireSetup = true,
                                onBack = { /* no back from forced setup */ },
                                onSaved = { navController.navigate("list") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }
                            )
                        }

                        composable("create") {
                            CreateInstanceScreen(
                                repository = app.appContainer.repository,
                                onBack = { navController.popBackStack() },
                                onCreated = { navController.navigate("list") { popUpTo("list") { inclusive = true } } }
                            )
                        }
                    }
                }
            }
        }
    }
}
