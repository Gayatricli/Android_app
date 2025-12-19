package com.example.stressease.Analytics

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stressease.R
import com.google.android.material.button.MaterialButton

class Breathing : AppCompatActivity() {

    // UI
    private lateinit var btnBack: ImageView
    private lateinit var breathingCircle: View
    private lateinit var tvBreathingPhase: TextView
    private lateinit var tvBreathingTimer: TextView
    private lateinit var tvCycleCounter: TextView

    private lateinit var btnStart: MaterialButton
    private lateinit var btnPause: MaterialButton
    private lateinit var btnStop: MaterialButton

    private lateinit var completionOverlay: View
    private lateinit var btnFinish: MaterialButton

    // Breathing config
    private val inhaleTime = 4000L
    private val holdTime = 2000L
    private val exhaleTime = 4000L
    private val totalCycles = 5

    // State
    private var currentCycle = 0
    private var isPaused = false
    private var currentAnimator: ValueAnimator? = null
    private var currentTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.breathing)

        bindViews()
        setupActions()
        resetUI()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        breathingCircle = findViewById(R.id.breathingCircle)
        tvBreathingPhase = findViewById(R.id.tvBreathingPhase)
        tvBreathingTimer = findViewById(R.id.tvBreathingTimer)
        tvCycleCounter = findViewById(R.id.tvCycleCounter)

        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)

        completionOverlay = findViewById(R.id.completionOverlay)
        btnFinish = findViewById(R.id.btnFinish)
    }

    private fun setupActions() {

        btnBack.setOnClickListener {
            finish()
        }

        btnStart.setOnClickListener {
            startBreathing()
        }

        btnPause.setOnClickListener {
            togglePause()
        }

        btnStop.setOnClickListener {
            stopBreathing()
        }

        btnFinish.setOnClickListener {
            finish()
        }
    }

    /* ---------------- CORE LOGIC ---------------- */

    private fun startBreathing() {
        btnStart.visibility = View.GONE
        btnPause.visibility = View.VISIBLE
        btnStop.visibility = View.VISIBLE

        tvBreathingTimer.visibility = View.VISIBLE
        tvCycleCounter.visibility = View.VISIBLE

        currentCycle = 0
        completionOverlay.visibility = View.GONE

        runCycle()
    }

    private fun runCycle() {
        if (currentCycle >= totalCycles) {
            showCompletion()
            return
        }

        currentCycle++
        tvCycleCounter.text = "Cycle $currentCycle of $totalCycles"

        inhale()
    }

    private fun inhale() {
        tvBreathingPhase.text = "Inhale"
        animateCircle(1f, 1.25f, inhaleTime)
        startTimer(inhaleTime) {
            hold()
        }
    }

    private fun hold() {
        tvBreathingPhase.text = "Hold"
        startTimer(holdTime) {
            exhale()
        }
    }

    private fun exhale() {
        tvBreathingPhase.text = "Exhale"
        animateCircle(1.25f, 1f, exhaleTime)
        startTimer(exhaleTime) {
            runCycle()
        }
    }

    /* ---------------- ANIMATION ---------------- */

    private fun animateCircle(from: Float, to: Float, duration: Long) {
        currentAnimator?.cancel()

        currentAnimator = ObjectAnimator.ofFloat(breathingCircle, "scaleX", from, to).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(breathingCircle, "scaleY", from, to).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    /* ---------------- TIMER ---------------- */

    private fun startTimer(duration: Long, onFinish: () -> Unit) {
        currentTimer?.cancel()

        currentTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvBreathingTimer.text = ((millisUntilFinished / 1000) + 1).toString()
            }

            override fun onFinish() {
                tvBreathingTimer.text = "0"
                onFinish()
            }
        }.start()
    }

    /* ---------------- PAUSE / STOP ---------------- */

    private fun togglePause() {
        if (isPaused) {
            isPaused = false
            btnPause.text = "Pause"
            runCycle()
        } else {
            isPaused = true
            btnPause.text = "Resume"
            currentAnimator?.cancel()
            currentTimer?.cancel()
            tvBreathingPhase.text = "Paused"
        }
    }

    private fun stopBreathing() {
        currentAnimator?.cancel()
        currentTimer?.cancel()
        resetUI()
    }

    private fun resetUI() {
        btnStart.visibility = View.VISIBLE
        btnPause.visibility = View.GONE
        btnStop.visibility = View.GONE

        tvBreathingPhase.text = "Ready"
        tvBreathingTimer.visibility = View.GONE
        tvCycleCounter.visibility = View.GONE

        breathingCircle.scaleX = 1f
        breathingCircle.scaleY = 1f

        completionOverlay.visibility = View.GONE
        isPaused = false
    }

    private fun showCompletion() {
        currentAnimator?.cancel()
        currentTimer?.cancel()
        completionOverlay.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        currentAnimator?.cancel()
        currentTimer?.cancel()
    }
}
