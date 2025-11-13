package com.example.stressease.Settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavController

@Composable
fun EditProfileScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Edit Profile Screen", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun StressGoalsScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Stress Goals Screen", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Notification Settings Screen", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun PrivacySecurityScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Privacy & Security Screen", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun DataStorageScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Data Storage Screen", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AboutSupportScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Help • Terms • About",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Help center / Contact us / About the app",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
