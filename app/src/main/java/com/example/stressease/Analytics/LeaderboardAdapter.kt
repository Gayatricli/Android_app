package com.example.stressease.Analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R



class LeaderboardAdapter(
    private val items: List<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.LBViewHolder>() {

    class LBViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank = view.findViewById<TextView>(R.id.tvRank)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvMoodEmoji = view.findViewById<TextView>(R.id.tvMoodEmoji)
        val tvScore = view.findViewById<TextView>(R.id.tvScore)
        val tvMoodCount = view.findViewById<TextView>(R.id.tvMoodCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LBViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LBViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: LBViewHolder, position: Int) {
        val item = items[position]

        holder.tvRank.text = item.rank
        holder.tvUsername.text = item.username
        holder.tvMoodEmoji.text = item.emoji
        holder.tvScore.text = item.score.toString()
        holder.tvMoodCount.text = "${item.logs} mood logs"
    }
}
