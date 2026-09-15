package com.l1khith.calender28.service

/**
 * Pure wall-clock timing and state engine for focus sessions.
 * Decoupled from Android Context, Services, and UI to ensure Single Responsibility
 * and complete JVM unit testability.
 */
class FocusTimerEngine(
    val mode: String, // "timer" or "stopwatch"
    val targetDurationSeconds: Int = 0,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    var sessionStartTimeMs: Long = clock()
        private set
    var lastResumeTimestampMs: Long = sessionStartTimeMs
        private set
    var accumulatedElapsedSeconds: Int = 0
        private set
    var isPaused: Boolean = false
        private set

    init {
        start()
    }

    fun start() {
        val now = clock()
        sessionStartTimeMs = now
        lastResumeTimestampMs = now
        accumulatedElapsedSeconds = 0
        isPaused = false
    }

    fun pause(): Int {
        if (!isPaused) {
            val now = clock()
            val segmentSec = ((now - lastResumeTimestampMs) / 1000L).toInt().coerceAtLeast(0)
            accumulatedElapsedSeconds += segmentSec
            isPaused = true
        }
        return accumulatedElapsedSeconds
    }

    fun resume() {
        if (isPaused) {
            lastResumeTimestampMs = clock()
            isPaused = false
        }
    }

    fun currentElapsedSeconds(): Int {
        if (isPaused) return accumulatedElapsedSeconds
        val now = clock()
        val segmentSec = ((now - lastResumeTimestampMs) / 1000L).toInt().coerceAtLeast(0)
        return accumulatedElapsedSeconds + segmentSec
    }

    fun remainingSeconds(): Int {
        val elapsed = currentElapsedSeconds()
        return (targetDurationSeconds - elapsed).coerceAtLeast(0)
    }

    fun isFinished(): Boolean {
        return mode == "timer" && targetDurationSeconds > 0 && currentElapsedSeconds() >= targetDurationSeconds
    }
}
