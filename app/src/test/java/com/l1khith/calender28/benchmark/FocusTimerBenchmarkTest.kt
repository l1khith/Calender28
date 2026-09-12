package com.l1khith.calender28.benchmark

import com.l1khith.calender28.service.FocusTimerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class FocusTimerBenchmarkTest {

    @Test
    fun `benchmark FocusTimerEngine tick calculation throughput and drift-free SLA`() {
        var simulatedTime = 1_000_000L
        val engine = FocusTimerEngine(
            mode = "timer",
            targetDurationSeconds = 100_000,
            clock = { simulatedTime }
        )

        val iterations = 100_000

        val elapsedMs = measureTimeMillis {
            repeat(iterations) {
                simulatedTime += 1000L // +1 sec
                val elapsed = engine.currentElapsedSeconds()
                val remaining = engine.remainingSeconds()
                if (elapsed % 10_000 == 0) {
                    engine.pause()
                    simulatedTime += 5000L // 5s sleep while paused
                    engine.resume()
                }
            }
        }

        val opsPerSec = (iterations.toDouble() / elapsedMs.coerceAtLeast(1)) * 1000
        println("⚡ [BENCHMARK] FocusTimerEngine: $iterations evaluations in ${elapsedMs}ms (~${opsPerSec.toInt()} ops/sec)")

        // Drift verification: Exactly 100,000 elapsed seconds
        assertEquals(100_000, engine.currentElapsedSeconds())
        assertEquals(0, engine.remainingSeconds())
        assertTrue("FocusTimerEngine tick calculation must execute under 500ms for 100,000 iterations", elapsedMs < 500)
    }
}
