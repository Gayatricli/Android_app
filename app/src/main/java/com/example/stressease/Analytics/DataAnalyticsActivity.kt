package com.example.stressease.Analytics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.LoginMain.LoginActivity
import com.example.stressease.R
import com.google.android.material.button.MaterialButton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class DataAnalyticsActivity : AppCompatActivity() {

    // Summary
    private lateinit var tvAvgMood: TextView
    private lateinit var tvAvgStress: TextView
    private lateinit var tvDominantIssue: TextView
    private lateinit var cardBreathing: View

    // Trends
    private lateinit var tvMoodTrend: TextView
    private lateinit var tvStressTrend: TextView

    // Prediction
    private lateinit var tvPredictionState: TextView
    private lateinit var tvPredictionConfidence: TextView
    private lateinit var tvPredictionReason: TextView

    // Android-only recommendations
    private lateinit var tvRecommendation1: TextView
    private lateinit var tvRecommendation2: TextView

    // Buttons
    private lateinit var btnBack: MaterialButton
    private lateinit var btnFinish: MaterialButton

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_analytics)

        bindViews()
        setAndroidRecommendations()   // ✅ NOT FROM FLASK
        callFinalSummaryEndpoint()
        setupActions()
    }

    private fun bindViews() {

        // Summary
        tvAvgMood = findViewById(R.id.tvAvgMood)
        tvAvgStress = findViewById(R.id.tvAvgStress)
        tvDominantIssue = findViewById(R.id.tvDominantIssue)

        // Trends
        tvMoodTrend = findViewById(R.id.tvMoodTrend)
        tvStressTrend = findViewById(R.id.tvStressTrend)

        // Prediction
        tvPredictionState = findViewById(R.id.tvPredictionState)
        tvPredictionConfidence = findViewById(R.id.tvPredictionConfidence)
        tvPredictionReason = findViewById(R.id.tvPredictionReason)

        // Recommendations
        tvRecommendation1 = findViewById(R.id.tvRecommendation1)
        tvRecommendation2 = findViewById(R.id.tvRecommendation2)

        // Buttons
        btnBack = findViewById(R.id.btnBack)
        btnFinish = findViewById(R.id.btnExportData)

        cardBreathing = findViewById(R.id.cardBreathing)
    }

    /**
     * Android-only UX nudges
     * NO backend dependency
     */
    private fun setAndroidRecommendations() {
        tvRecommendation1.text =
            "Use the StressEase chatbot for emotional support"
        tvRecommendation2.text =
            "Try a short breathing or grounding exercise"
    }

    /**
     * POST /analytics/final-summary
     * Android sends NO metrics
     * Flask fetches Firestore data internally
     */
    private fun callFinalSummaryEndpoint() {

        val emptyBody =
            RequestBody.create("application/json".toMediaType(), "{}")

        val request = Request.Builder()
            .url("http://YOUR_FLASK_IP:5000/analytics/final-summary")
            .post(emptyBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DataAnalytics", "Final summary API failed", e)
            }

            override fun onResponse(call: Call, response: Response) {

                if (!response.isSuccessful) return

                val json = JSONObject(response.body!!.string())

                runOnUiThread {

                    /* -------- SUMMARY -------- */
                    val summary = json.getJSONObject("summary")
                    tvAvgMood.text = summary.optString("avg_mood", "--")
                    tvAvgStress.text = summary.optString("avg_stress", "--")
                    tvDominantIssue.text =
                        summary.optString("dominant_issue", "--")
                            .replaceFirstChar { it.uppercase() }

                    /* -------- TRENDS -------- */
                    val trends = json.getJSONObject("trends")
                    tvMoodTrend.text =
                        formatTrend(trends.optString("mood"))
                    tvStressTrend.text =
                        formatTrend(trends.optString("stress"))

                    /* -------- PREDICTION -------- */
                    val prediction = json.getJSONObject("prediction")

                    tvPredictionState.text =
                        prediction.optString("state")
                            .replace("_", " ")
                            .replaceFirstChar { it.uppercase() }

                    tvPredictionConfidence.text =
                        "(${prediction.optString("confidence")} confidence)"

                    tvPredictionReason.text =
                        prediction.optString("reason")
                }
            }
        })
    }

    private fun formatTrend(value: String): String {
        return when (value.lowercase()) {
            "increasing" -> "↑ Increasing"
            "declining" -> "↓ Declining"
            else -> "→ Stable"
        }
    }

    private fun setupActions() {

        btnBack.setOnClickListener {
            finish()
        }

        btnFinish.setOnClickListener {
            val intent = Intent(
                this@DataAnalyticsActivity,
                LoginActivity::class.java
            )
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        tvRecommendation1.setOnClickListener {
            val intent = Intent(
                this@DataAnalyticsActivity,
                com.example.stressease.chats.ChatActivity::class.java
            )
            startActivity(intent)
        }

        cardBreathing.setOnClickListener {
            val intent = Intent(
                this@DataAnalyticsActivity,
                com.example.stressease.Analytics.Breathing::class.java
            )
            startActivity(intent)
        }

    }
}
