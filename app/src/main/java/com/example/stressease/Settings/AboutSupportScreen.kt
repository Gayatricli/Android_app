package com.example.stressease.Settings

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSupportScreen(navController: NavController) {

    val ctx = LocalContext.current

    var showHelpDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & Support", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text("StressEase App", fontWeight = FontWeight.Bold)
            Text("Version 2.4.1", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(20.dp))

            Text("Support", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            AboutRow("Help Center") { showHelpDialog = true }
            AboutRow("Terms & Conditions") { showTermsDialog = true }
            AboutRow("Privacy Policy") { showPrivacyDialog = true }
            AboutRow("Frequently Asked Questions") { showFaqDialog = true }

            AboutRow("Contact Support") {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:stressease.support@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Support Request")
                }
                ctx.startActivity(intent)
            }

            Spacer(Modifier.height(20.dp))

            Text("Developer Info", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Developed with ❤️ for wellbeing.")
            Text("Email: stressease.support@gmail.com")
        }
    }

    // ------------------- DIALOGS -------------------

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help Center") },
            text = {
                Text("We are building our online help center. For now, reach us at:\n\nstressease.support@gmail.com")
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions") },
            text = {
                Text("Our terms and conditions will be available soon.\n\nFor any queries, contact support.")
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text("The privacy policy is under development.\n\nWe prioritize user data security.")
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("FAQ") },
            text = {
                Text(
                    """
                    • How does StressEase analyze moods?
                      → Using mood logs, quizzes, and trends.

                    • Are my chats stored?
                      → Yes, securely in Firestore.

                    • Will more features be added?
                      → Yes, upcoming updates will include reminders & more.
                    """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AboutRow(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Text("›", fontWeight = FontWeight.Bold) // simple arrow
    }
}
