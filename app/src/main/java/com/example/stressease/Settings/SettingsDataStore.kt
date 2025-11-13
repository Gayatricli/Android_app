package com.example.stressease.Settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("settings_prefs")

class SettingsDataStore(private val context: Context) {

    companion object {
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val WEEKLY_GOAL = intPreferencesKey("weekly_goal")
        val PUSH = booleanPreferencesKey("push")
        val EMAIL_NOTIF = booleanPreferencesKey("email_notif")
        val REMINDERS = booleanPreferencesKey("reminders")
        val BIOMETRIC = booleanPreferencesKey("biometric")
        val DATA_SHARING = booleanPreferencesKey("data_sharing")
    }

    val settingsFlow: Flow<SettingsModel> = context.settingsDataStore.data
        .map { prefs ->
            SettingsModel(
                name = prefs[NAME] ?: "",
                email = prefs[EMAIL] ?: "",
                dailyGoal = prefs[DAILY_GOAL] ?: 3,
                weeklyGoal = prefs[WEEKLY_GOAL] ?: 15,
                pushEnabled = prefs[PUSH] ?: true,
                emailEnabled = prefs[EMAIL_NOTIF] ?: false,
                remindersEnabled = prefs[REMINDERS] ?: true,
                biometricLock = prefs[BIOMETRIC] ?: false,
                dataSharing = prefs[DATA_SHARING] ?: true
            )
        }

    suspend fun saveSettings(settings: SettingsModel) {
        context.settingsDataStore.edit { prefs ->
            prefs[NAME] = settings.name
            prefs[EMAIL] = settings.email
            prefs[DAILY_GOAL] = settings.dailyGoal
            prefs[WEEKLY_GOAL] = settings.weeklyGoal
            prefs[PUSH] = settings.pushEnabled
            prefs[EMAIL_NOTIF] = settings.emailEnabled
            prefs[REMINDERS] = settings.remindersEnabled
            prefs[BIOMETRIC] = settings.biometricLock
            prefs[DATA_SHARING] = settings.dataSharing
        }
    }
    suspend fun clear() {
        context.settingsDataStore.edit { it.clear() }
    }
}
