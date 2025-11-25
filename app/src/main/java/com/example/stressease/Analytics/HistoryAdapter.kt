package com.example.stressease.History

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R

// Reuse the existing simple ChatHistoryItem (flat-list) for backwards compatibility:
data class FlatChatHistoryItem(
    val userMessage: String,
    val botReply: String,
    val timestamp: Long
)

// New models for analytics session usage:
data class SessionMessage(
    val sender: String,
    val message: String,
    val emotion: String,
    val sentimentScore: Double = 0.0,
    val timestamp: Long = 0L
)

data class ChatSession(
    val sessionId: String,
    val messages: List<SessionMessage>
)

/**
 * Adapter supports three view types:
 * - VIEW_TYPE_HEADER: session header (when using sessions)
 * - VIEW_TYPE_USER: user message (old layout item_chat_user)
 * - VIEW_TYPE_BOT: bot message (old layout item_chat_bot)
 *
 * Backwards-compatible methods:
 * - setData(List<FlatChatHistoryItem>)  -> old behavior
 * - setSessionData(List<ChatSession>)   -> new analytics grouping behavior
 */
class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    // Internal flat list for rendering: Pair(viewType, payload)
    // payload is a String for messages, or String header text for headers
    private val renderList = mutableListOf<Pair<Int, Any>>()

    // -------------------------
    // Public API (backwards)
    // -------------------------
    fun setData(pairedChats: List<FlatChatHistoryItem>) {
        renderList.clear()
        for (chat in pairedChats) {
            if (chat.userMessage.isNotBlank()) {
                renderList.add(VIEW_TYPE_USER to chat.userMessage)
            }
            if (chat.botReply.isNotBlank()) {
                renderList.add(VIEW_TYPE_BOT to chat.botReply)
            }
        }
        notifyDataSetChanged()
    }

    // -------------------------
    // New API: sessions for analytics
    // -------------------------
    fun setSessionData(sessions: List<History.ChatSession>) {
        renderList.clear()
        // iterate sessions and flatten with header
        sessions.forEach { session ->
            // Add header
            renderList.add(VIEW_TYPE_HEADER to "Session: ${session.sessionId}")
            // Add messages in chronological order
            session.messages.sortedBy { it.timestamp }.forEach { msg ->
                if (msg.sender.equals("user", ignoreCase = true)) {
                    renderList.add(VIEW_TYPE_USER to msg.message)
                } else {
                    // treat as bot by default
                    renderList.add(VIEW_TYPE_BOT to msg.message)
                }
            }
        }
        notifyDataSetChanged()
    }

    // -------------------------
    // RecyclerView methods
    // -------------------------
    override fun getItemViewType(position: Int): Int = renderList[position].first

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                // inflate a simple header layout (see suggested XML below)
                val view = inflater.inflate(R.layout.item_session_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_USER -> {
                val view = inflater.inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_chat_bot, parent, false)
                BotViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = renderList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (type, payload) = renderList[position]
        when (type) {
            VIEW_TYPE_HEADER -> {
                (holder as HeaderViewHolder).bind(payload as String)
            }
            VIEW_TYPE_USER -> {
                (holder as UserViewHolder).bind(payload as String)
            }
            VIEW_TYPE_BOT -> {
                (holder as BotViewHolder).bind(payload as String)
            }
        }
    }

    // -------------------------
    // ViewHolders
    // -------------------------
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvSessionHeader)
        fun bind(text: String) {
            tvHeader.text = text
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userText: TextView = itemView.findViewById(R.id.tvUserMessage)
        fun bind(text: String) {
            userText.text = text
        }
    }

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val botText: TextView = itemView.findViewById(R.id.tvBotMessage)
        fun bind(text: String) {
            botText.text = text
        }
    }
}
