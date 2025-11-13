package com.example.stressease.Analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R

class LeaderboardAdapter(private val leaderboardList: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val entry = leaderboardList[position]
        holder.tvRank.text = "#${position + 1}"
        holder.tvUsername.text = entry.username
        holder.tvScore.text = "Score: ${entry.score}"
    }

    override fun getItemCount(): Int{
        return leaderboardList.size
    }
}