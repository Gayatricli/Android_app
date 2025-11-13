package com.example.stressease.Settings

data class SettingsModel(
    val name: String = "",
    val email: String = "",
    val dailyGoal: Int = 3,
    val weeklyGoal: Int = 15,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
    val remindersEnabled: Boolean = true,
    val biometricLock: Boolean = false,
    val dataSharing: Boolean = true,
    val lastSyncedMillis: Long = 0L
)
