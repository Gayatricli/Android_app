package com.example.stressease.Analytics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class Summary : AppCompatActivity() {

    // Simple Layout IDs
    private lateinit var tvSummaryTitle: TextView
    private lateinit var tvTotalChats: TextView
    private lateinit var tvTotalMoods: TextView
    private lateinit var tvMostCommonEmotion: TextView
    private lateinit var tvMostCommonMood: TextView
    private lateinit var tvOverallStatus: TextView
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button

    // Firebase + API
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary)

        initViews()
        loadPlaceholders()

        loadFirestoreSummary()     // Firestore data
        loadStressPrediction()     // AI prediction (placeholder-safe)

        btnBack.setOnClickListener { finish() }
        btnNext.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
    }

    private fun initViews() {
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle)
        tvTotalChats = findViewById(R.id.tvTotalChats)
        tvTotalMoods = findViewById(R.id.tvTotalMoods)
        tvMostCommonEmotion = findViewById(R.id.tvMostCommonEmotion)
        tvMostCommonMood = findViewById(R.id.tvMostCommonMood)
        tvOverallStatus = findViewById(R.id.tvOverallStatus)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
    }

    private fun loadPlaceholders() {
        tvSummaryTitle.text = "Overall Mood and Chat Summary"
        tvTotalChats.text = "Total Chats: 0"
        tvTotalMoods.text = "Total Moods Logged: 0"
        tvMostCommonEmotion.text = "Most Common Emotion: None"
        tvMostCommonMood.text = "Most Common Mood: None"
        tvOverallStatus.text = "Overall Status: Mixed Mood"
    }

    // ---------------------------
    // FIRESTORE SUMMARY SECTION
    // ---------------------------
    private fun loadFirestoreSummary() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val chats = doc.getLong("totalChats") ?: 0
                val moods = doc.getLong("totalMoods") ?: 0
                val emotion = doc.getString("mostCommonEmotion") ?: "None"
                val mood = doc.getString("mostCommonMood") ?: "None"
                val status = doc.getString("overallStatus") ?: "Mixed Mood"

                tvTotalChats.text = "Total Chats: $chats"
                tvTotalMoods.text = "Total Moods Logged: $moods"
                tvMostCommonEmotion.text = "Most Common Emotion: $emotion"
                tvMostCommonMood.text = "Most Common Mood: $mood"
                tvOverallStatus.text = "Overall Status: $status"
            }
            .addOnFailureListener {
                Log.e("SUMMARY", "Failed to load", it)
            }
    }

    // ---------------------------
    // FLASK STRESS PREDICTION
    // Placeholder-safe
    // ---------------------------
    private fun loadStressPrediction() {


        val url = "https://your-flask-api.com/predict"

        val json = JSONObject().apply {
            put("user_id", auth.currentUser?.uid ?: "")
        }

        val req = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), json.toString()))
            .build()

        httpClient.newCall(req).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PREDICT", "API unreachable (placeholder mode)", e)

            }

            override fun onResponse(call: Call, response: Response) {
                // ✔ Response processed only if valid JSON
                val body = response.body?.string() ?: return
                try {
                    val obj = JSONObject(body)
                    val overall = obj.optString("overall_status", "Mixed Mood")

                    runOnUiThread {
                        tvOverallStatus.text = "Overall Status: $overall"
                    }

                } catch (e: Exception) {
                    Log.e("PREDICT", "JSON parse error", e)
                }
            }
        })
    }
}
