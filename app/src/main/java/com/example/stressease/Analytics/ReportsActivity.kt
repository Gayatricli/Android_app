package com.example.stressease.Analytics

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.LocalStorageOffline.SharedPreference
import com.example.stressease.LoginMain.MainActivity
import com.example.stressease.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var tvReportTitle: TextView
    private lateinit var tvReportStats: TextView
    private lateinit var tvReportStatus: TextView
    private lateinit var emotionBarChart: BarChart
    private lateinit var moodPieChart: PieChart
    private lateinit var moodTrendChart: LineChart
    private lateinit var btnBack: Button
    private lateinit var btnViewSummary: Button
    private lateinit var btnNext: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reports)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind views
        tvReportTitle = findViewById(R.id.tvReportTitle)
        tvReportStats = findViewById(R.id.tvReportStats)
        tvReportStatus = findViewById(R.id.tvReportStatus)
        emotionBarChart = findViewById(R.id.emotionBarChart)
        moodPieChart = findViewById(R.id.moodPieChart)
        moodTrendChart = findViewById(R.id.moodTrendChart)
        btnBack = findViewById(R.id.btnBack)
        btnViewSummary = findViewById(R.id.btnViewSummary)
        btnNext = findViewById(R.id.btnNext)

        // Navigation
        btnBack.setOnClickListener { finish() }
        btnViewSummary.setOnClickListener {
            startActivity(Intent(this, Summary::class.java))
        }
        btnNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Fetch weekly user-specific reports
        loadWeeklyUserReport()
    }

    /**
     * Fetches only last 7 days of data for the current user from Firestore.
     * Combines mood logs and quiz scores into weekly summaries.
     */
    private fun loadWeeklyUserReport() {
        val userId = auth.currentUser?.uid ?: return
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time

        db.collection("users")
            .document(userId)
            .collection("moods")
            .whereGreaterThan("timestamp", sevenDaysAgo)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    tvReportStats.text = "No mood data found for this week ⚠️"
                    Log.w("ReportsActivity", "No weekly mood data found.")
                    return@addOnSuccessListener
                }

                val moodList = snapshot.documents.mapNotNull { it.getString("mood") }
                val quizScores = snapshot.documents.mapNotNull { it.getDouble("quiz_score") }

                val total = moodList.size
                val positive = moodList.count { it.equals("Happy", true) || it.equals("Calm", true) || it.equals("Excited", true) }
                val negative = moodList.count { it.equals("Sad", true) || it.equals("Angry", true) || it.equals("Stressed", true) }
                val neutral = total - positive - negative

                val avgQuiz = if (quizScores.isNotEmpty()) quizScores.average() else 0.0

                tvReportStats.text = """
                    Weekly Summary (${SimpleDateFormat("MMM dd", Locale.getDefault()).format(sevenDaysAgo)} - Today)
                    Total Entries: $total
                    Positive: $positive
                    Negative: $negative
                    Neutral: $neutral
                    Avg Quiz Score: ${String.format("%.1f", avgQuiz)}
                """.trimIndent()

                tvReportStatus.text = when {
                    positive > negative && positive > neutral -> "You maintained a positive state this week 🎉"
                    negative > positive && negative > neutral -> "This week was emotionally challenging 😟"
                    else -> "Your week had mixed emotions ⚖️"
                }

                val counts = mapOf("Positive" to positive, "Negative" to negative, "Neutral" to neutral)
                showEmotionBarChart(counts)
                showMoodPieChart(counts)
                showMoodTrendChart(moodList)

                // Save weekly summary in Firestore for analytics
                saveWeeklySummary(userId, total, positive, negative, neutral, avgQuiz, moodList)
            }
            .addOnFailureListener { e ->
                tvReportStats.text = "Failed to load weekly data ❌ ${e.message}"
                Log.e("ReportsActivity", "Firestore error", e)
            }
    }

    private fun saveWeeklySummary(
        userId: String,
        total: Int,
        positive: Int,
        negative: Int,
        neutral: Int,
        avgQuiz: Double,
        moodList: List<String>
    ) {
        val summaryData = hashMapOf(
            "totalMoods" to total,
            "positiveCount" to positive,
            "negativeCount" to negative,
            "neutralCount" to neutral,
            "averageQuizScore" to avgQuiz,
            "last7DaysMood" to moodList,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("reports")
            .document("weekly_summary")
            .set(summaryData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ReportsActivity", "✅ Weekly summary saved.")
            }
            .addOnFailureListener { e ->
                Log.e("ReportsActivity", "❌ Failed to save weekly summary: ${e.message}", e)
            }
    }

    private fun showEmotionBarChart(emotionCounts: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        var index = 0
        for ((_, count) in emotionCounts) {
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
            index++
        }

        val dataSet = BarDataSet(entries, "Emotions")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 16f

        val data = BarData(dataSet)
        emotionBarChart.data = data
        emotionBarChart.description = Description().apply { text = "" }
        emotionBarChart.animateY(1000)
        emotionBarChart.invalidate()
    }

    private fun showMoodPieChart(emotionCounts: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((emotion, count) in emotionCounts) {
            entries.add(PieEntry(count.toFloat(), emotion))
        }

        val dataSet = PieDataSet(entries, "Mood Distribution")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 18f

        val data = PieData(dataSet)
        moodPieChart.data = data
        moodPieChart.description = Description().apply { text = "" }
        moodPieChart.animateY(1000)
        moodPieChart.invalidate()
    }

    private fun showMoodTrendChart(moodList: List<String>) {
        val entries = ArrayList<Entry>()
        for ((index, mood) in moodList.withIndex()) {
            val value = when (mood) {
                "Happy" -> 3f
                "Calm" -> 2.5f
                "Neutral" -> 2f
                "Sad" -> 1f
                "Angry" -> 0.5f
                else -> 2f
            }
            entries.add(Entry(index.toFloat(), value))
        }

        val dataSet = LineDataSet(entries, "Mood Trend (7 Days)")
        dataSet.colors = listOf(ColorTemplate.getHoloBlue())
        dataSet.circleColors = listOf(ColorTemplate.getHoloBlue())
        dataSet.valueTextSize = 14f
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 4f

        val data = LineData(dataSet)
        moodTrendChart.data = data
        moodTrendChart.axisLeft.axisMinimum = 0f
        moodTrendChart.axisLeft.axisMaximum = 3.5f
        moodTrendChart.description = Description().apply { text = "" }
        moodTrendChart.legend.textSize = 14f
        moodTrendChart.animateX(1000)
        moodTrendChart.invalidate()
    }
}
