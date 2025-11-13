package com.example.stressease.Analytics


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Leaderboard : AppCompatActivity() {
    private lateinit var recyclerLeaderboard: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val leaderboardList = mutableListOf<LeaderboardEntry>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.leaderboard)

        recyclerLeaderboard = findViewById(R.id.recyclerLeaderboard)
        recyclerLeaderboard.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(leaderboardList)
        recyclerLeaderboard.adapter = adapter

        loadLeaderboard()
    }
    private fun loadLeaderboard() {
        db.collection("users")
            .orderBy(
                "score",
                Query.Direction.DESCENDING
            ) // sort by score
            .limit(20) // top 20 users
            .get()
            .addOnSuccessListener { documents ->
                leaderboardList.clear()
                var rank = 1
                for (doc in documents) {
                    val username = doc.getString("email") ?: "Unknown"
                    val score = doc.getLong("score") ?: 0
                    leaderboardList.add(LeaderboardEntry(rank.toString(), username, score.toInt()))
                    rank++
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Leaderboard", "Error fetching leaderboard", e)
            }
    }

}