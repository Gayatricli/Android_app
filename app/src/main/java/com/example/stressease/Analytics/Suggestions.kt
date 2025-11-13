package com.example.stressease.Analytics

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Suggestions : AppCompatActivity() {

    private lateinit var tvEmotion: TextView
    private lateinit var tvSummary: TextView
    private lateinit var tvMotivation: TextView
    private lateinit var tvSuggestions: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: ImageButton

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var insightsListener: ListenerRegistration? = null
    private var aiDataLoaded = false  // ✅ flag to confirm if Flask/Firestore data is real

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestion)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvEmotion = findViewById(R.id.tvEmotion)
        tvSummary = findViewById(R.id.tvSummary)
        tvMotivation = findViewById(R.id.tvMotivation)
        tvSuggestions = findViewById(R.id.tvSuggestions)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { confirmAndSaveInsights() }

        loadAIInsightsFromFirestore()
    }

    override fun onDestroy() {
        super.onDestroy()
        insightsListener?.remove()
    }

    private fun loadAIInsightsFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val insightsRef = db.collection("users")
            .document(userId)
            .collection("ai_insights")
            .document("latest")

        insightsListener = insightsRef.addSnapshotListener { doc, e ->
            if (e != null) {
                Log.e("SuggestionsActivity", "❌ Firestore listener failed: ${e.message}")
                Toast.makeText(this, "Error loading insights.", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (doc == null || !doc.exists()) {
                tvSummary.text = "No insights yet — complete a quiz to generate your AI analysis!"
                aiDataLoaded = false
                return@addSnapshotListener
            }

            val emotion = doc.getString("dominant_emotion") ?: "Neutral"
            val summary = doc.getString("summary") ?: ""
            val confidence = doc.getDouble("confidence_score") ?: 0.0
            val motivation = doc.getString("motivation_quote") ?: ""
            val suggestionsList = doc.get("suggestions") as? List<String> ?: emptyList()

            val suggestionsFormatted =
                if (suggestionsList.isNotEmpty()) suggestionsList.joinToString("\n• ", prefix = "• ")
                else "No personalized suggestions available yet."

            tvEmotion.text = "Dominant Emotion: $emotion\nConfidence: ${String.format("%.0f", confidence)}%"
            tvSummary.text = summary.ifBlank { "No summary data available." }
            tvMotivation.text = motivation.ifBlank { "Keep calm and stay motivated 💪" }
            tvSuggestions.text = suggestionsFormatted

            aiDataLoaded = summary.isNotBlank() || suggestionsList.isNotEmpty()

            Log.d("SuggestionsActivity", "✅ Loaded insights for $userId → $emotion ($confidence%)")
        }
    }

    private fun confirmAndSaveInsights() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "⚠️ Please log in first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if AI data really exists
        if (!aiDataLoaded) {
            Toast.makeText(this, "⚠️ No AI insights available to save yet.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "emotion" to tvEmotion.text.toString(),
            "summary" to tvSummary.text.toString(),
            "motivation" to tvMotivation.text.toString(),
            "suggestions" to tvSuggestions.text.toString(),
            "savedAt" to System.currentTimeMillis()
        )
        db.collection("users")
            .document(userId)
            .collection("saved_insights")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "💾 AI insights saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SuggestionsActivity", "Error saving insights", e)
            }
    }
}
