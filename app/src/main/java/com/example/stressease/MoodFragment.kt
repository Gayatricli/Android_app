package com.example.stressease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.stressease.Analytics.QuizFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoodFragment : Fragment() {

    private lateinit var switchMoodQuiz: SwitchMaterial
    private lateinit var spinnerMood: Spinner
    private lateinit var analyzeBtn: Button
    private lateinit var result: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerMood = view.findViewById(R.id.spinnerMood)
        switchMoodQuiz = view.findViewById(R.id.switchMoodQuiz)
        analyzeBtn = view.findViewById(R.id.analyzeBtn)
        result = view.findViewById(R.id.resultView)

        setupMoodSpinner()
        setupSwitch()
        setupAnalyzeButton()
    }

    // ------------------ Spinner Setup ------------------
    private fun setupMoodSpinner() {
        val moods = listOf(
            "😊 Happy",
            "😔 Sad",
            "😌 Calm",
            "😡 Angry",
            "🤩 Excited",
            "😴 Tired",
            "😣 Stressed",
            "🙂 Neutral"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, // your gradient/padded layout
            moods
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMood.adapter = adapter
    }

    // ------------------ Switch Setup ------------------
    private fun setupSwitch() {
        switchMoodQuiz.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Smoothly open QuizFragment
                val quizFragment = QuizFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, quizFragment)
                    .addToBackStack(null)
                    .commit()
                switchMoodQuiz.isChecked = false // reset toggle visually
            }
        }
    }

    // ------------------ Analyze Button Logic ------------------
    private fun setupAnalyzeButton() {
        analyzeBtn.setOnClickListener {
            val selectedMood = spinnerMood.selectedItem?.toString()?.trim() ?: ""

            if (selectedMood.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a mood", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val moodMessage = when {
                selectedMood.contains("Happy") -> "😊 Great! Keep spreading positivity."
                selectedMood.contains("Sad") -> "😔 Take some rest and listen to calming music."
                selectedMood.contains("Angry") -> "😤 Try deep breathing to relax your mind."
                selectedMood.contains("Stressed") -> "😩 A short walk or meditation might help."
                selectedMood.contains("Neutral") -> "🙂 Stay calm and balanced throughout your day."
                else -> "😐 Your mood seems balanced today."
            }

            result.text = moodMessage
            saveMoodToFirestore(selectedMood)
        }
    }

    // ------------------ Firestore Logic ------------------
    private fun saveMoodToFirestore(selectedMood: String) {
        val userId = auth.currentUser?.uid ?: return
        val moodData = hashMapOf(
            "mood" to selectedMood,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("moods")
            .add(moodData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Mood saved ✅", Toast.LENGTH_SHORT).show()
                generateInstantWeeklyReport(userId)
            }
            .addOnFailureListener {
                result.text = "❌ Error saving mood: ${it.message}"
            }
    }

    // ------------------ Weekly Summary ------------------
    private fun generateInstantWeeklyReport(userId: String) {
        db.collection("users").document(userId).collection("moods")
            .get()
            .addOnSuccessListener { snapshot ->
                val moodCounts = mutableMapOf<String, Int>()
                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000

                for (doc in snapshot) {
                    val mood = doc.getString("mood") ?: continue
                    val timestamp = doc.getLong("createdAt") ?: continue
                    if (timestamp >= sevenDaysAgo) {
                        moodCounts[mood] = (moodCounts[mood] ?: 0) + 1
                    }
                }

                if (moodCounts.isNotEmpty()) {
                    val total = moodCounts.values.sum()
                    val topMood = moodCounts.maxByOrNull { it.value }?.key ?: "Neutral"
                    val topMoodPercent = (moodCounts[topMood]!! * 100 / total)

                    val reportText = buildString {
                        append("\n📊 Weekly Mood Report:\n")
                        append("• Dominant Mood: $topMood ($topMoodPercent%)\n")
                        append("• Total Logs: $total\n\n🗓 Breakdown:\n")
                        moodCounts.forEach { (mood, count) ->
                            val bar = "█".repeat((count * 5 / total).coerceAtLeast(1))
                            append("$mood — $bar ($count)\n")
                        }
                        append("\n💡 Insight: ${getMoodInsight(topMood)}")
                    }
                    result.text = reportText
                } else {
                    result.text = "No mood logs for this week yet."
                }
            }
    }

    private fun getMoodInsight(mood: String): String {
        return when {
            mood.contains("Happy") -> "You’ve been in a positive state overall this week!"
            mood.contains("Sad") -> "Try journaling or light activity to lift your spirits."
            mood.contains("Stressed") -> "Take short breaks and focus on calm breathing."
            mood.contains("Angry") -> "Redirect your energy with exercise or creativity."
            else -> "You seem balanced this week — great consistency!"
        }
    }
}
