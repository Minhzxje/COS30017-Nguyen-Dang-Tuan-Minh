package com.example.climbscoreapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Button
import android.util.Log
import androidx.core.content.ContextCompat
import android.view.View

const val MAX_SCORE = 18
const val MIN_SCORE = 0

class MainActivity : AppCompatActivity() {
    private lateinit var scoreManager: ScoreManager
    private lateinit var btnClimb: Button
    private lateinit var btnFall: Button
    private lateinit var btnReset: Button
    private lateinit var text1: TextView
    private lateinit var score: TextView

    private lateinit var warningText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        scoreManager = ScoreManager()

        score = findViewById(R.id.score)
        btnClimb = findViewById(R.id.btnClimb)
        btnFall = findViewById(R.id.btnFall)
        btnReset = findViewById(R.id.btnReset)
        text1 = findViewById(R.id.text1)
        warningText = findViewById(R.id.warningText)

        if (savedInstanceState != null) {
            scoreManager.apply {
                score = savedInstanceState.getInt("score")
                currentHold = savedInstanceState.getInt("currentHold")
                climbedToTop = savedInstanceState.getBoolean("climbedToTop")
                hasFallen = savedInstanceState.getBoolean("hasFallen")
            }
            updateUI()
        }

        btnClimb.setOnClickListener {
            scoreManager.climb()
            Log.d("Climb", "Hold: ${scoreManager.currentHold}, Score: ${scoreManager.score}")
            updateUI()
        }

        btnFall.setOnClickListener {
            scoreManager.fall()
            Log.d("Fall", "Hold: ${scoreManager.currentHold}, Score: ${scoreManager.score}")
            updateUI()
        }

        btnReset.setOnClickListener {
            scoreManager.reset()
            Log.d("Reset", "Reset game")
            updateUI()
        }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("score", scoreManager.score)
        outState.putInt("currentHold", scoreManager.currentHold)
        outState.putBoolean("climbedToTop", scoreManager.climbedToTop)
        outState.putBoolean("hasFallen", scoreManager.hasFallen)
    }

    private fun updateUI() {
        score.text = scoreManager.score.toString()

        val color = when (scoreManager.currentHold) {
            in 1..3 -> R.color.blue_zone
            in 4..6 -> R.color.green_zone
            in 7..9 -> R.color.red_zone
            else -> R.color.blue_zone
        }

        score.setTextColor(ContextCompat.getColor(this, color))

        warningText.visibility = View.GONE
        if (scoreManager.currentHold == 0) {
            btnFall.isEnabled = false
            warningText.text = getString(R.string.warning_need_climb)
            warningText.visibility = View.VISIBLE
        }
        else {
            btnFall.isEnabled = true
        }

        if (scoreManager.hasFallen) {
            btnClimb.isEnabled = false
            warningText.text = getString(R.string.warning_after_fall)
            warningText.visibility = View.VISIBLE
        }
        else if (scoreManager.currentHold < 9) {
            btnClimb.isEnabled = true
        }

        if (scoreManager.climbedToTop) {
            btnFall.isEnabled = false
            warningText.text = getString(R.string.warning_at_top)
            warningText.visibility = View.VISIBLE
        }
    }
}