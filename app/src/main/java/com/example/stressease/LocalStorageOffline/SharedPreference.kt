package com.example.stressease.LocalStorageOffline

import android.content.Context
import com.example.stressease.chats.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPreference {
    private const val PREFS_NAME = "StressEasePrefs"

    fun saveChatList(context: Context, key: String, list: List<ChatMessage>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun loadChatList(context: Context, key: String): MutableList<ChatMessage> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
        return Gson().fromJson(json, type)
    }

    // 🔹 Save/Load String Lists (moods, journals, suggestions, etc.)
    fun saveStringList(context: Context, key: String, list: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun loadStringList(context: Context, key: String): MutableList<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // 🔹 Save/Load Integer Values
    fun saveInt(context: Context, key: String, value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, default: Int = 0): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, default)
    }

    // 🔹 Save/Load Score List (Quiz results, etc.)
    fun saveScoreList(context: Context, key: String, list: List<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(list)
        prefs.edit().putString(key, json).apply()
    }

    fun loadScoreList(context: Context, key: String): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
