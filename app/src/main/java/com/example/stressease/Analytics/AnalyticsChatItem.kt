package com.example.stressease.Analytics

data class AnalyticsChatItem(
    val sender: String,
    val message: String,
    val emotion: String,
    val sentimentScore: Double,
    val timestamp: Long
)