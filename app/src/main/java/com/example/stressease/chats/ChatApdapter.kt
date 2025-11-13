package com.example.stressease.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R

class ChatAdapter(private val messages: List<ChatMessage>):

    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val VIEW_TYPE_USER = 1
        private val VIEW_TYPE_BOT = 2
        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_USER) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_bot, parent, false)
                BotViewHolder(view)
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            if (holder is UserViewHolder) {
                holder.bind(message)
            } else if (holder is BotViewHolder) {
                holder.bind(message)
            }
        }
        override fun getItemCount(): Int = messages.size

        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
            fun bind(message: ChatMessage) {
                tvMessage.text = message.text
            }
        }
        inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessage: TextView = itemView.findViewById(R.id.tvBotMessage)
            fun bind(message: ChatMessage) {
                tvMessage.text = message.text

                if (message.emotion == "error") {
                    tvMessage.setBackgroundResource(R.drawable.bg_bot_message)
                } else {
                    tvMessage.setBackgroundResource(R.drawable.bg_bot_message)
                }
            }

        }
    }


