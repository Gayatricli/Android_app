package com.example.stressease.chats

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stressease.Api.ChatRequest
import com.example.stressease.Api.RetrofitClient
import com.example.stressease.History.History
import com.example.stressease.LocalStorageOffline.SharedPreference
import com.example.stressease.SOS.SOS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke

// 🎨 Color Palette
object ChatColors {
    val BackgroundTop = Color(0xFFF5E6D3)
    val BackgroundMid = Color(0xFFE8D8C8)
    val BackgroundMidBlue = Color(0xFFD8E5F0)
    val BackgroundBottom = Color(0xFFB8D8E8)
    val Surface = Color(0xFFFFFBF5)
    val SurfaceBorder = Color(0xFFD0DCE8)
    val Primary = Color(0xFF6B4423)
    val PrimaryLight = Color(0xFF8D6E63)
    val TextPrimary = Color(0xFF5D4037)
    val TextSecondary = Color(0xFF8D6E63)
    val UserBubble = Color(0xFF6B4423)
    val BotBubble = Color(0xFFFFFBF5)
    val BotAvatar = Color(0xFF8BC34A)
    val UserTimestamp = Color(0xFFD4B896)
    val BotTimestamp = Color(0xFF8D6E63)
    val InputBorder = Color(0xFFD4C4B0)
    val SmileyGradientStart = Color(0xFFF4C430)
    val SmileyGradientEnd = Color(0xFFFFA500)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFragmentCompose(isNewSession: Boolean = false) {
    val context = LocalContext.current

    // State management
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputValue by remember { mutableStateOf("") }
    var currentSessionId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Firebase instances
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // Load chat history
    LaunchedEffect(Unit) {
        messages = if (isNewSession) {
            SharedPreference.saveChatList(context, "chat_history", mutableListOf())
            emptyList()
        } else {
            SharedPreference.loadChatList(context, "chat_history")
        }
    }

    // Auto-scroll
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun addMessage(chatMessage: ChatMessage) {
        messages = messages + chatMessage
        SharedPreference.saveChatList(context, "chat_history", messages.toMutableList())
    }

    fun saveChatMessage(chatMessage: ChatMessage) {
        val userId = auth.currentUser?.uid ?: return
        val chatData = hashMapOf(
            "message" to chatMessage.message.ifEmpty { chatMessage.text },
            "sender" to if (chatMessage.isUser) "user" else "bot",
            "emotion" to chatMessage.emotion,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("chats")
            .add(chatData)
            .addOnSuccessListener {
                Log.d("Firestore", "Saved: ${chatData["sender"]} → ${chatData["message"]}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to save chat", e)
            }
    }

    fun sendMessage(userMessage: String) {
        val prefs = context.getSharedPreferences("AppPrefs", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("authToken", null)

        if (token == null) {
            Toast.makeText(context, "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ChatRequest(userMessage, session_id = currentSessionId)
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.sendMessage("Bearer $token", request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val chatResp = response.body()!!
                        val botReply = chatResp.ai_response?.content ?: "No reply"
                        val botMessage = ChatMessage(
                            text = botReply,
                            isUser = false,
                            emotion = chatResp.ai_response?.role ?: "assistant",
                            message = botReply
                        )
                        addMessage(botMessage)
                        saveChatMessage(botMessage)
                    } else {
                        val fallback = ChatMessage(
                            text = "I’m here for you, even offline 😊",
                            isUser = false,
                            emotion = "assistant",
                            message = "offline"
                        )
                        addMessage(fallback)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ChatFragment", "Server error: ${e.message}")
                }
            }
        }
    }

    // 🧩 Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        ChatColors.BackgroundTop,
                        ChatColors.BackgroundMid,
                        ChatColors.BackgroundMidBlue,
                        ChatColors.BackgroundBottom
                    )
                )
            )
    ) {
        // Header stays same
        ChatHeader(
            onSOS = {
                context.startActivity(Intent(context, SOS::class.java))
            },
            onHistory = {
                context.startActivity(Intent(context, History::class.java))
            }
        )

        // Center smiley background + chat area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // 🌸 Decorative centered smiley
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "😊",
                    fontSize = 200.sp,
                    color = Color.Black.copy(alpha = 0.20f),
                    modifier = Modifier.align(Alignment.Center)
                )

                val decor = listOf(
                    Pair("🌸", Modifier.offset((-120).dp, (-150).dp)),
                    Pair("✨", Modifier.offset(130.dp, (-100).dp)),
                    Pair("☀️", Modifier.offset((-80).dp, 80.dp)),
                    Pair("💫", Modifier.offset(120.dp, 100.dp)),
                    Pair("🍃", Modifier.offset((-140).dp, 150.dp)),
                    Pair("🌈", Modifier.offset(100.dp, (-160).dp))
                )
                decor.forEach { (emoji, mod) ->
                    Text(
                        text = emoji,
                        fontSize = 36.sp,
                        color = Color.Black.copy(alpha = 0.18f),
                        modifier = mod
                    )
                }
            }

            // Chat messages list (above background)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message = message)
                }
            }
        }

        // Input section
        ChatInputSection(
            value = inputValue,
            onValueChange = { inputValue = it },
            onSend = {
                if (inputValue.trim().isNotEmpty()) {
                    val chat = ChatMessage(
                        text = inputValue,
                        isUser = true,
                        emotion = "neutral",
                        message = inputValue
                    )
                    addMessage(chat)
                    saveChatMessage(chat)
                    val messageToSend = inputValue
                    inputValue = ""
                    sendMessage(messageToSend)
                }
            }
        )

        // Navigation bar
        NavigationBar(
            containerColor = ChatColors.Surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = false,
                onClick = {
                    val intent = Intent(context, com.example.stressease.LoginMain.MainActivity::class.java)
                    context.startActivity(intent)
                }
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                label = { Text("Chat") },
                selected = true,
                onClick = {
                    messages = emptyList()
                    SharedPreference.saveChatList(context, "chat_history", mutableListOf())
                    Toast.makeText(context, "Chat cleared!", Toast.LENGTH_SHORT).show()
                }
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                selected = false,
                onClick = {
                    Toast.makeText(context, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ChatHeader(
    onSOS: () -> Unit,
    onHistory: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(ChatColors.Surface),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    ChatColors.SmileyGradientStart,
                                    ChatColors.SmileyGradientEnd
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfied,
                        contentDescription = "Smiley",
                        tint = ChatColors.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Stress Ease",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChatColors.TextPrimary
                    )
                    Text(
                        text = "Your wellness companion",
                        fontSize = 12.sp,
                        color = ChatColors.TextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSOS,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("SOS", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onHistory,
                    contentPadding = PaddingValues(8.dp),
                    border = BorderStroke(1.dp, ChatColors.TextSecondary.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChatColors.TextPrimary)
                ) {
                    Icon(Icons.Default.History, contentDescription = "History", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    val isUser = message.isUser
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = remember(message.timestamp) {
        try {
            timeFormat.format(Date(message.timestamp.toLong()))
        } catch (e: Exception) {
            "--:--"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ChatColors.BotAvatar, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "Bot", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 16.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isUser) ChatColors.UserBubble else ChatColors.BotBubble,
                shadowElevation = if (isUser) 0.dp else 1.dp,
                border = if (!isUser) BorderStroke(1.dp, ChatColors.SurfaceBorder) else null
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else Color(0xFF4A4A4A),
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = if (isUser) ChatColors.UserTimestamp else ChatColors.BotTimestamp
                    )
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ChatColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "User", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ChatColors.Surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = ChatColors.TextSecondary,
                        unfocusedIndicatorColor = ChatColors.InputBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4
                )

                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(44.dp),
                    containerColor = ChatColors.Primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Press Enter to send • Shift + Enter for new line",
                fontSize = 11.sp,
                color = ChatColors.TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
