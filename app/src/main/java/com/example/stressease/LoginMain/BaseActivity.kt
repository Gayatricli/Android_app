package com.example.stressease.LoginMain

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.chats.ChatActivity
import com.example.stressease.MoodFragment
import com.example.stressease.R
import com.example.stressease.Analytics.ReportsActivity
import com.example.stressease.Analytics.Suggestions
import com.google.android.material.bottomnavigation.BottomNavigationView


class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    protected fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, MoodFragment::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_analytics -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, Suggestions::class.java))
                    true
                }
                else -> false
            }
        }
    }
}