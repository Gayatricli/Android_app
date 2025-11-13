package com.example.stressease.History

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R

data class ChatHistoryItem(
    val userMessage: String,
    val botReply: String,
    val timestamp: Long
)

class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    private val chatItems = mutableListOf<Pair<Int, String>>() // (viewType, message)

    override fun getItemViewType(position: Int): Int = chatItems[position].first

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (_, message) = chatItems[position]
        if (holder is UserViewHolder) {
            holder.userText.text = message
        } else if (holder is BotViewHolder) {
            holder.botText.text = message
        }
    }

    override fun getItemCount(): Int = chatItems.size

    fun setData(pairedChats: List<ChatHistoryItem>) {
        chatItems.clear()
        for (chat in pairedChats) {
            if (chat.userMessage.isNotBlank())
                chatItems.add(VIEW_TYPE_USER to chat.userMessage)
            if (chat.botReply.isNotBlank())
                chatItems.add(VIEW_TYPE_BOT to chat.botReply)
        }
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userText: TextView = itemView.findViewById(R.id.tvUserMessage)
    }

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val botText: TextView = itemView.findViewById(R.id.tvBotMessage)
    }
}
