package com.example.stressease.Analytics

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.stressease.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class QuizFragment : Fragment() {

    private val TAG = "QuizFragment"

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI
    private lateinit var tvQuestion: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCategory: TextView
    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: MaterialButton

    // option includes (we support up to 5; XML may have 4 or 5)
    private val layoutOpts = mutableListOf<LinearLayout>()
    private val radioOpts = mutableListOf<RadioButton>()
    private val checkmarkTxts = mutableListOf<TextView>()
    private val badgeTxts = mutableListOf<TextView>()

    // Data
    private var currentIndex = 0
    private val questions = mutableListOf<Map<String, Any>>()
    private val selectedOptionIndex = mutableMapOf<Int, Int>() // index -> 1..5

    // grouped payload pieces
    private val coreScores = mutableMapOf<String, Int>()
    private val rotatingScores = mutableListOf<Int>()
    private val dassToday = mutableMapOf<String, Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_quiz, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        bindViews(view)
        setupListeners()
        loadQuestionsForToday()
        return view
    }

    private fun bindViews(root: View) {
        tvQuestion = root.findViewById(R.id.tvQuestion)
        tvProgress = root.findViewById(R.id.tvProgress)
        progressBar = root.findViewById(R.id.progressBar)
        tvCategory = root.findViewById(R.id.tvCategory)
        btnNext = root.findViewById(R.id.btnNext)
        btnBack = root.findViewById(R.id.btnBack)

        // Clear previous lists (safe if rebind)
        layoutOpts.clear(); radioOpts.clear(); checkmarkTxts.clear(); badgeTxts.clear()

        // Try to bind up to 5 includes. If layoutOpt5 not present, we keep 4.
        for (i in 1..5) {
            val lid = resources.getIdentifier("layoutOpt$i", "id", requireContext().packageName)
            if (lid != 0) {
                val layout = root.findViewById<LinearLayout>(lid)
                layoutOpts.add(layout)

                // radioOption inside include
                val radio = layout.findViewById<RadioButton>(R.id.radioOption)
                radioOpts.add(radio)

                val check = layout.findViewById<TextView>(R.id.tvCheckmark)
                checkmarkTxts.add(check)

                val badge = layout.findViewById<TextView>(R.id.tvOptionNumber)
                badgeTxts.add(badge)
            }
        }

        // If badges present, label them 1..N
        badgeTxts.forEachIndexed { idx, tv -> tv.text = (idx + 1).toString() }

        progressBar.max = 100
        progressBar.progress = 0
        btnBack.isEnabled = false
        btnNext.isEnabled = false
        btnNext.alpha = 0.6f
    }

    private fun setupListeners() {
        // attach click listeners for each option include (1..N)
        for (i in layoutOpts.indices) {
            val score = i + 1 // option value 1..N
            layoutOpts[i].setOnClickListener {
                selectOption(score)
            }
            // also allow clicking the radio (just in case)
            radioOpts.getOrNull(i)?.setOnClickListener {
                selectOption(score)
            }
        }

        btnNext.setOnClickListener { goNext() }
        btnBack.setOnClickListener { goBack() }
    }

    private fun getDayKey(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "day_1"
            Calendar.TUESDAY -> "day_2"
            Calendar.WEDNESDAY -> "day_3"
            Calendar.THURSDAY -> "day_4"
            Calendar.FRIDAY -> "day_5"
            Calendar.SATURDAY -> "day_6"
            else -> "day_7"
        }
    }

    private fun loadQuestionsForToday() {
        val dayKey = getDayKey()

        db.collection("questions").document(dayKey).get()
            .addOnSuccessListener { doc ->
                val data = doc.get("questions")
                questions.clear()

                when (data) {
                    is List<*> -> {
                        for (item in data) {
                            if (item is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                questions.add(item as Map<String, Any>)
                            }
                        }
                    }
                    is Map<*, *> -> {
                        // Firestore sometimes stores arrays as maps with numeric keys
                        val entries = data.entries.toList().sortedBy { e ->
                            e.key?.toString()?.toIntOrNull() ?: 0
                        }
                        for ((_, v) in entries) {
                            if (v is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                questions.add(v as Map<String, Any>)
                            }
                        }
                    }
                    else -> {
                        Log.e(TAG, "loadQuestionsForToday: unexpected data type ${data?.javaClass}")
                    }
                }

                Log.d(TAG, "Loaded ${questions.size} questions for $dayKey")
                if (questions.isNotEmpty()) {
                    currentIndex = 0
                    // make sure UI updates on main loop
                    view?.post { showQuestion(currentIndex) }
                } else {
                    Toast.makeText(requireContext(), "No questions found for $dayKey", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore error", e)
                Toast.makeText(requireContext(), "Error loading quiz: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showQuestion(index: Int) {
        if (index !in questions.indices) return

        val q = questions[index]
        val text = q["text"]?.toString()?.takeIf { it.isNotBlank() } ?: "Question unavailable"
        val optionsRaw = q["options"]
        val options = when (optionsRaw) {
            is List<*> -> optionsRaw.map { it.toString() }
            is Map<*, *> -> optionsRaw.entries.sortedBy { it.key.toString().toIntOrNull() ?: 0 }
                .map { it.value.toString() }
            else -> emptyList()
        }

        val category = q["dimension"]?.toString() ?: "General"

        tvQuestion.text = text
        tvCategory.text = category

        // fill option texts for available option count (N)
        val nOptions = radioOpts.size // typically 5
        val displayed = MutableList(nOptions) { idx -> options.getOrNull(idx) ?: (idx + 1).toString() }

        for (i in 0 until nOptions) {
            radioOpts[i].text = displayed[i]
            radioOpts[i].isChecked = false
            checkmarkTxts.getOrNull(i)?.visibility = View.GONE
        }

        // restore selection if exists
        val prev = selectedOptionIndex[index]
        if (prev != null && prev in 1..nOptions) {
            val idx = prev - 1
            radioOpts[idx].isChecked = true
            checkmarkTxts.getOrNull(idx)?.visibility = View.VISIBLE
            btnNext.isEnabled = true
            btnNext.alpha = 1f
        } else {
            btnNext.isEnabled = false
            btnNext.alpha = 0.6f
        }

        btnBack.isEnabled = index > 0
        btnBack.alpha = if (btnBack.isEnabled) 1f else 0.6f

        btnNext.text = if (index == questions.size - 1) "Submit" else "Next"

        val progressValue = if (questions.isNotEmpty()) ((index + 1) * 100) / questions.size else 0
        tvProgress.text = "Question ${index + 1} of ${questions.size}"
        animateProgress(progressValue)

        // ensure layout refresh; helps prevent a stuck UI on some devices
        view?.post {
            view?.invalidate()
            view?.requestLayout()
        }
    }

    private fun selectOption(score: Int) {
        val n = radioOpts.size
        if (score !in 1..n) return

        // hide all checkmarks and uncheck other radios
        for (i in radioOpts.indices) {
            radioOpts[i].isChecked = false
            checkmarkTxts.getOrNull(i)?.visibility = View.GONE
        }

        // set the chosen one
        val idx = score - 1
        radioOpts[idx].isChecked = true
        checkmarkTxts.getOrNull(idx)?.visibility = View.VISIBLE

        selectedOptionIndex[currentIndex] = score

        // enable Next
        btnNext.isEnabled = true
        btnNext.alpha = 1f
    }

    private fun goNext() {
        // ensure selection present
        if (!selectedOptionIndex.containsKey(currentIndex)) {
            Toast.makeText(requireContext(), "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentIndex < questions.size - 1) {
            currentIndex++
            // post UI update to ensure safe redraw
            view?.post {
                showQuestion(currentIndex)
            }
        } else {
            // final submit
            mapAnswersAndSubmit()
        }
    }

    private fun goBack() {
        if (currentIndex > 0) {
            currentIndex--
            view?.post {
                showQuestion(currentIndex)
            }
        }
    }

    private fun animateProgress(target: Int) {
        val anim = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, target)
        anim.duration = 350
        anim.interpolator = DecelerateInterpolator()
        anim.start()
    }

    private fun mapAnswersAndSubmit() {
        val total = questions.size
        val all = (0 until total).map { selectedOptionIndex[it] ?: 0 }

        coreScores.clear(); rotatingScores.clear(); dassToday.clear()

        if (total >= 12) {
            coreScores["mood"] = all.getOrNull(0) ?: 0
            coreScores["energy"] = all.getOrNull(1) ?: 0
            coreScores["sleep"] = all.getOrNull(2) ?: 0
            coreScores["stress"] = all.getOrNull(3) ?: 0

            // rotating 5 values (indexes 4..8)
            for (i in 4..8) rotatingScores.add(all.getOrNull(i) ?: 0)

            dassToday["depression"] = all.getOrNull(9) ?: 0
            dassToday["anxiety"] = all.getOrNull(10) ?: 0
            dassToday["stress"] = all.getOrNull(11) ?: 0
        } else {
            // fallback: map q1..qn into core_scores if nonstandard length
            for ((i, v) in all.withIndex()) coreScores["q${i + 1}"] = v
        }

        submitDailyMoodQuiz()
    }

    private fun submitDailyMoodQuiz() {
        val user = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        user.getIdToken(true).addOnSuccessListener { tokenResult ->
            val token = tokenResult.token ?: return@addOnSuccessListener
            val client = OkHttpClient()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val payload = JSONObject().apply {
                put("core_scores", JSONObject(coreScores as Map<*, *>))
                put("rotating_scores", JSONObject().apply {
                    put("domain_name", chooseRotatingDomain())
                    put("scores", JSONArray(rotatingScores))
                })
                put("dass_today", JSONObject(dassToday as Map<*, *>))
                put("date", date)
                put("day_key", getDayKey())
                put("additional_notes", "Submitted via Android app")
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://d1bace5fbd5a.ngrok-free.app/api/mood/quiz/daily")
                .addHeader("Authorization", "Bearer $token")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "submitDailyMoodQuiz: ${e.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Submission failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val bodyStr = response.body?.string()
                    Log.d(TAG, "submitDailyMoodQuiz response: $bodyStr")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Quiz submitted!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), com.example.stressease.Analytics.Suggestions::class.java))
                    }
                }
            })
        }
    }

    private fun chooseRotatingDomain(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "physical"
            Calendar.TUESDAY -> "social"
            Calendar.WEDNESDAY -> "work"
            Calendar.THURSDAY -> "emotional"
            Calendar.FRIDAY -> "cognitive"
            Calendar.SATURDAY -> "spiritual"
            else -> "other"
        }
    }
}
