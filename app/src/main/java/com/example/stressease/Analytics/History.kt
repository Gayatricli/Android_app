package com.example.stressease.History

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class History : AppCompatActivity() {
    data class FlatChatHistoryItem(
        val userMessage: String,
        val botReply: String,
        val timestamp: Long
    )
    // New models for analytics session usage:

    data class ChatSession(
        val sessionId: String,
        val messages: List<AnalyticsChatItem> = emptyList()
    )
    data class AnalyticsChatItem(
        val sender: String,
        val message: String,
        val emotion: String,
        val sentimentScore: Double,
        val timestamp: Long
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        loadSessionsForAnalytics()
    }

    private fun loadSessionsForAnalytics() {
        db.collection("users")
            .document(userId)
            .collection("chats")
            .orderBy("session_id", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null || snapshot.isEmpty) {
                    adapter.setSessionData(emptyList())
                    return@addOnSuccessListener
                }

                val groups = snapshot.documents.groupBy {
                    it.getString("session_id") ?: "unknown_session"
                }

                val formattedSessions = groups.map { (sessionId, docs) ->
                    ChatSession(
                        sessionId = sessionId,
                        messages = docs.map { doc ->
                            AnalyticsChatItem(
                                sender = doc.getString("sender") ?: "",
                                message = doc.getString("message") ?: "",
                                emotion = doc.getString("emotion") ?: "",
                                sentimentScore = doc.getDouble("sentiment_score") ?: 0.0,
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        }
                    )
                }

                adapter.setSessionData(formattedSessions)
            }
            .addOnFailureListener {
                Log.e("History", "Error loading chats: ${it.message}")
            }
    }

}
