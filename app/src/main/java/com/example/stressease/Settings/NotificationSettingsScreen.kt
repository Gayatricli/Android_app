import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stressease.Settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController, viewModel: SettingsViewModel) {

    val ui by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    var push by remember { mutableStateOf(ui.pushEnabled) }
    var email by remember { mutableStateOf(ui.emailEnabled) }
    var reminders by remember { mutableStateOf(ui.remindersEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(Modifier.padding(padding).padding(16.dp)) {

            SettingSwitch("Push Notifications", push) { push = it }
            SettingSwitch("Email Notifications", email) { email = it }
            SettingSwitch("Daily Reminders", reminders) { reminders = it }

            Spacer(Modifier.height(20.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.updateAndSave(
                        ui.copy(
                            pushEnabled = push,
                            emailEnabled = email,
                            remindersEnabled = reminders
                        )
                    )
                    Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            ) { Text("Save") }
        }
    }
}



