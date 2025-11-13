package com.example.stressease.Settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class SettingsActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "settings"
                ) {

                    composable("settings") {
                        SettingsScreen(
                            navController = navController,
                            viewModel = settingsViewModel
                        )
                    }

                    composable("editProfile") {
                        EditProfileScreen(
                            navController = navController,
                            viewModel = settingsViewModel
                        )
                    }

                    composable("stressGoals") {
                        StressGoalsScreen(
                            navController = navController,
                            viewModel = settingsViewModel
                        )
                    }

                    composable("notificationSettings") {
                        NotificationSettingsScreen(
                            navController = navController,
                            viewModel = settingsViewModel
                        )
                    }

                    composable("privacySecurity") {
                        PrivacySecurityScreen(
                            navController = navController,
                            viewModel = settingsViewModel
                        )
                    }

                    composable("dataStorage") {
                        DataStorageScreen(
                            navController = navController,
                            viewModel = settingsViewModel
                        )
                    }

                    composable("aboutSupport") {
                        AboutSupportScreen(navController = navController)
                    }
                }
            }
        }
    }
}
