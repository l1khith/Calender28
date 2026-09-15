package com.l1khith.calender28.benchmark

import com.l1khith.calender28.utils.HabitCycleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class HabitCycleBenchmarkTest {

    @Test
    fun `benchmark HabitCycleEngine position computation throughput`() {
        val iterations = 100_000
        val anchorDay = 0L

        // Warmup
        repeat(5_000) {
            HabitCycleEngine.computePosition(it.toLong(), anchorDay)
        }

        val elapsedMs = measureTimeMillis {
            repeat(iterations) { i ->
                val pos = HabitCycleEngine.computePosition(i.toLong(), anchorDay)
                if (i == 27) {
                    assertEquals(0L, pos.cycleIndex)
                    assertEquals(28, pos.dayInCycle)
                }
            }
        }

        val opsPerSec = (iterations.toDouble() / elapsedMs.coerceAtLeast(1)) * 1000
        println("⚡ [BENCHMARK] HabitCycleEngine: $iterations position calculations in ${elapsedMs}ms (~${opsPerSec.toInt()} ops/sec)")

        // SLA: 100,000 calculations must take under 200ms
        assertTrue("Habit cycle position calculation too slow: ${elapsedMs}ms", elapsedMs < 200)
    }
}
