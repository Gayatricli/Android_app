package com.example.stressease.LoginMain

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.emailInputLayout)
        passwordInput = findViewById(R.id.passwordInputLayout)
        loginBtn = findViewById(R.id.loginButton)
        registerBtn = findViewById(R.id.signUpText)

        pref=getSharedPreferences("AppPrefs", MODE_PRIVATE)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                saveToken()
            } else {
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                saveToken()
            } else {
                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToken() {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val idToken = result.token
                if (!idToken.isNullOrEmpty()) {
                    val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                    prefs.edit().putString("authToken", idToken).apply()
                    Log.d("LoginActivity", "Token saved: $idToken")

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    window.decorView.post {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

                } else {
                    Toast.makeText(this, "Token fetch failed", Toast.LENGTH_SHORT).show()
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("LoginActivity", "Token fetch error: ${e.message}")
                Toast.makeText(this, "Failed to fetch token", Toast.LENGTH_SHORT).show()
            }
    }
}




