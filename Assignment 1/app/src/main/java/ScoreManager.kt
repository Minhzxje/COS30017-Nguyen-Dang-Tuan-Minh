package com.example.climbscoreapp
class ScoreManager {
    var score = 0
    var currentHold = 0
    var climbedToTop = false
    var hasFallen = false

    fun climb() {
        if (hasFallen || climbedToTop) return

        if (currentHold < 9) {
            currentHold++
            score += when (currentHold) {
                in 1..3 -> 1
                in 4..6 -> 2
                in 7..9 -> 3
                else -> 0
            }
            if (score > MAX_SCORE) score = MAX_SCORE
            if (currentHold == 9) climbedToTop = true
        }
    }

    fun fall() {
        if (currentHold >= 1 && !climbedToTop) {
            score -= 3
            if (score < MIN_SCORE) score = MIN_SCORE
            hasFallen = true
        }
    }

    fun reset() {
        score = 0
        currentHold = 0
        climbedToTop = false
        hasFallen = false
    }
}