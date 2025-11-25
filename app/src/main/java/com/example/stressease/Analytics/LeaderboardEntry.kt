package com.example.stressease.Analytics

data class LeaderboardEntry(
    val rank: String,
    val username: String,
    val score: Int,
    val emoji: String,
    val logs: Int
)

