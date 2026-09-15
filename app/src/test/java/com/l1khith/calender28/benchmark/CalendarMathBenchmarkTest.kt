package com.l1khith.calender28.benchmark

import com.l1khith.calender28.utils.FixedCalendarHelper
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class CalendarMathBenchmarkTest {

    @Test
    fun `benchmark 28-day date conversion throughput`() {
        val iterations = 20_000

        // Warmup
        repeat(1_000) {
            val d = com.l1khith.calender28.utils.FixedDate(2026, 8, 14)
            val ts = FixedCalendarHelper.toTimestamp(d)
            FixedCalendarHelper.fromTimestamp(ts)
        }

        val elapsedMs = measureTimeMillis {
            repeat(iterations) { i ->
                val month = (i % 13) + 1
                val day = (i % 28) + 1
                val d = com.l1khith.calender28.utils.FixedDate(2026, month, day)
                val ts = FixedCalendarHelper.toTimestamp(d)
                val roundtrip = FixedCalendarHelper.fromTimestamp(ts)
                FixedCalendarHelper.addMonths(roundtrip, 1)
            }
        }

        val opsPerSec = (iterations.toDouble() / elapsedMs.coerceAtLeast(1)) * 1000
        println("⚡ [BENCHMARK] CalendarMath: $iterations timestamp round-trips & addMonths in ${elapsedMs}ms (~${opsPerSec.toInt()} ops/sec)")

        // SLA: 20,000 round-trips must finish in under 1,000ms
        assertTrue("Calendar date math too slow: ${elapsedMs}ms", elapsedMs < 1000)
    }
}
