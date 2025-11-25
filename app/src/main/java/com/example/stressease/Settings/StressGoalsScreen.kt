import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
fun StressGoalsScreen(navController: NavController, viewModel: SettingsViewModel) {

    val ui by viewModel.uiState.collectAsState()
    var daily by remember { mutableStateOf(ui.dailyGoal) }
    var weekly by remember { mutableStateOf(ui.weeklyGoal) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stress Goals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier.padding(padding).padding(16.dp)
        ) {

            Text("Daily Check-ins", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { if (daily > 0) daily-- }) { Text("-") }
                Spacer(Modifier.width(12.dp))
                Text(daily.toString())
                Spacer(Modifier.width(12.dp))
                Button(onClick = { daily++ }) { Text("+") }
            }

            Spacer(Modifier.height(20.dp))

            Text("Weekly Goal", fontWeight = FontWeight.SemiBold)
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
                    viewModel.updateAndSave(ui.copy(dailyGoal = daily, weeklyGoal = weekly))
                    Toast.makeText(ctx, "Saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Goals") }
        }
    }
}
