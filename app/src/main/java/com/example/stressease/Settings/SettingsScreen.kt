package com.example.stressease.Settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// ----------------------------------------------------
// MAIN SETTINGS SCREEN
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {

    val ctx = LocalContext.current
    val settings by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // ---------------- PROFILE CARD ----------------
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (settings.name.ifBlank { "U" }).take(2).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(settings.name.ifEmpty { "User" }, fontWeight = FontWeight.Bold)
                        Text(
                            settings.email.ifEmpty { "No email" },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = { navController.navigate("editProfile") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- NAV SECTIONS ----------------
            SectionCard("Account") {
                RowClickable("Stress Goals") { navController.navigate("stressGoals") }
            }
            SectionCard("Notifications") {
                RowClickable("Notification Preferences") { navController.navigate("notificationSettings") }
            }
            SectionCard("Privacy & Security") {
                RowClickable("Privacy Settings") { navController.navigate("privacySecurity") }
            }
            SectionCard("Data & Storage") {
                RowClickable("Export / Clear Cache") { navController.navigate("dataStorage") }
            }
            SectionCard("Support & About") {
                RowClickable("Help, Terms & About") { navController.navigate("aboutSupport") }
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- LOGOUT ----------------
            Button(
                onClick = {
                    scope.launch {
                        viewModel.signOut()
                        Toast.makeText(ctx, "Signed out", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }
        }
    }
}

// ----------------------------------------------------
// SECTION CARD
// ----------------------------------------------------
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), content = content)
    }
}

// ----------------------------------------------------
// CLICKABLE ROW
// ----------------------------------------------------
@Composable
fun RowClickable(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
    }
}

// ----------------------------------------------------
// EDIT PROFILE
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, viewModel: SettingsViewModel) {

    val settings by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf(settings.name) }
    var email by remember { mutableStateOf(settings.email) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile") })
        }
    ) { padding ->

        Column(
            Modifier.padding(padding).padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.updateAndSave(settings.copy(name = name.trim(), email = email.trim()))
                    Toast.makeText(ctx, "Updated", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}

// ----------------------------------------------------
// STRESS GOALS
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StressGoalsScreen(navController: NavController, viewModel: SettingsViewModel) {

    val settings by viewModel.uiState.collectAsState()
    var daily by remember { mutableStateOf(settings.dailyGoal) }
    var weekly by remember { mutableStateOf(settings.weeklyGoal) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stress Goals") }) }
    ) { padding ->

        Column(
            Modifier.padding(padding).padding(16.dp)
        ) {

            Text("Daily Check-ins")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { if (daily > 0) daily-- }) { Text("-") }
                Spacer(Modifier.width(12.dp))
                Text(daily.toString())
                Spacer(Modifier.width(12.dp))
                Button(onClick = { daily++ }) { Text("+") }
            }

            Spacer(Modifier.height(20.dp))

            Text("Weekly Goal")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { if (weekly > 1) weekly-- }) { Text("-") }
                Spacer(Modifier.width(12.dp))
                Text(weekly.toString())
                Spacer(Modifier.width(12.dp))
                Button(onClick = { weekly++ }) { Text("+") }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.updateAndSave(settings.copy(dailyGoal = daily, weeklyGoal = weekly))
                    Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Goals") }
        }
    }
}

// ----------------------------------------------------
// NOTIFICATION SETTINGS
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val settings by viewModel.uiState.collectAsState()

    var push by remember { mutableStateOf(settings.pushEnabled) }
    var email by remember { mutableStateOf(settings.emailEnabled) }
    var reminders by remember { mutableStateOf(settings.remindersEnabled) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notifications") }) }
    ) { padding ->

        Column(Modifier.padding(padding).padding(16.dp)) {

            SettingSwitch("Push Notifications", push) { push = it }
            SettingSwitch("Email Notifications", email) { email = it }
            SettingSwitch("Daily Reminders", reminders) { reminders = it }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.updateAndSave(
                        settings.copy(
                            pushEnabled = push,
                            emailEnabled = email,
                            remindersEnabled = reminders
                        )
                    )
                    Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}

// Setting Switch
@Composable
fun SettingSwitch(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}

// ----------------------------------------------------
// PRIVACY & SECURITY
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController, viewModel: SettingsViewModel) {

    val settings by viewModel.uiState.collectAsState()

    var biometric by remember { mutableStateOf(settings.biometricLock) }
    var dataSharing by remember { mutableStateOf(settings.dataSharing) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Privacy & Security") }) }
    ) { padding ->

        Column(Modifier.padding(padding).padding(16.dp)) {

            SettingSwitch("Biometric Lock", biometric) { biometric = it }
            SettingSwitch("Share anonymous data", dataSharing) { dataSharing = it }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.updateAndSave(
                        settings.copy(
                            biometricLock = biometric,
                            dataSharing = dataSharing
                        )
                    )
                    Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}

// ----------------------------------------------------
// DATA STORAGE
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(navController: NavController, viewModel: SettingsViewModel) {

    val ctx = LocalContext.current
    var exportedText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Data & Storage") }) }
    ) { padding ->

        Column(Modifier.padding(padding).padding(16.dp)) {

            Text("Export Settings")

            Button(
                onClick = {
                    scope.launch {
                        viewModel.exportSettings { success, json ->
                            exportedText = json
                            Toast.makeText(ctx, "Exported", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) { Text("Export") }

            exportedText?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.clearLocalCache()
                        Toast.makeText(ctx, "Cleared!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Clear Local", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}
