package com.example.stressease.Analytics

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.Analytics.ChatHistoryItem
import com.example.stressease.Analytics.HistoryAdapter
import com.example.stressease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ResourceHub : AppCompatActivity() {


}

class History: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var listenerRegistration: ListenerRegistration? = null
    private var userId: String = ""
    private val chatList = mutableListOf<ChatHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        // Start listening to Firestore in real-time
        listenToUserHistory()
    }

    private fun listenToUserHistory() {
        listenerRegistration = db.collection("users")
            .document(userId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener{ snapshots, e ->
                if (e != null) {
                    Log.e("HistoryActivity", "Error fetching data: ${e.message}")
                    Toast.makeText(this, "Error loading chats", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    chatList.clear() // Clear previous items

                    for (doc in snapshots.documents) {
                        val sender = doc.getString("sender")?.lowercase() ?: ""
                        val message = doc.getString("message") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: 0L

                        // Add each message as a new item
                        if (sender == "user") {
                            chatList.add(
                                ChatHistoryItem(
                                    userMessage = message,
                                    botReply = "",
                                    timestamp = timestamp
                                )
                            )
                        } else if (sender == "bot") {
                            chatList.add(
                                ChatHistoryItem(
                                    userMessage = "",
                                    botReply = message,
                                    timestamp = timestamp
                                )
                            )
                        }
                    }

                    // Update adapter once
                    adapter.setData(chatList)
                    recyclerView.scrollToPosition(chatList.size - 1)

                    Log.d("HistoryActivity", "Loaded ${chatList.size} messages")
                } else {
                    adapter.setData(emptyList())
                }
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove() // Stop Firestore updates
    }
}