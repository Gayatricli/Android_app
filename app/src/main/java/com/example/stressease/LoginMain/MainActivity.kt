package com.example.stressease.LoginMain

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.stressease.Analytics.ReportsActivity
import com.example.stressease.chats.ChatActivity
import com.example.stressease.MoodFragment
import com.example.stressease.R
import com.example.stressease.Settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val CHAT_TAG = "CHAT_FRAGMENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Default to home
        Handler(Looper.getMainLooper()).post {
            loadFragment(HomeFragment()) // or Splash/Home
            bottomNav.selectedItemId = R.id.nav_home

        }
        //  Handle first-time selection
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_mood -> {
                    loadFragment(MoodFragment())
                    true
                }

                R.id.nav_chat -> {
                    val chat1 = Intent(this, ChatActivity::class.java)
                    startActivity(chat1)
                    true
                }

                R.id.nav_analytics -> {
                    val act = Intent(this, ReportsActivity::class.java)
                    startActivity(act)
                    true
                }

                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // ✅ Handle re-selection (user taps Chat again)
        bottomNav.setOnItemReselectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_chat) {
                loadNewChatFragment() // Recreate ChatFragment again
            }
        }

    }

    // ✅ Always creates a new ChatFragment (clean chat)
    private fun loadNewChatFragment() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("isNewSession", true)

        // Clear the current ChatActivity if it's already open and start fresh
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
