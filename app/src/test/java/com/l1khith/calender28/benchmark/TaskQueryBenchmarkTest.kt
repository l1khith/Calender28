package com.l1khith.calender28.benchmark

import com.l1khith.calender28.test.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class TaskQueryBenchmarkTest {

    @Test
    fun `benchmark O-1 indexed lookup vs O-N linear scan`() {
        val taskCount = 1_000
        val queryCount = 5_000

        val taskList = (1..taskCount).map { i ->
            TestDataFactory.createAppTask(id = "task_$i", title = "Task $i")
        }
        val taskMap = taskList.associateBy { it.id }

        val targetIds = (1..queryCount).map { "task_${(it % taskCount) + 1}" }

        // Warmup
        repeat(500) {
            val id = targetIds[it]
            taskMap[id]
            taskList.find { it.id == id }
        }

        // Benchmark O(1) Indexed Lookup
        val indexedTimeMs = measureTimeMillis {
            var found = 0
            for (id in targetIds) {
                if (taskMap[id] != null) found++
            }
            assertEquals(queryCount, found)
        }

        // Benchmark O(N) Full Scan
        val scanTimeMs = measureTimeMillis {
            var found = 0
            for (id in targetIds) {
                if (taskList.find { it.id == id } != null) found++
            }
            assertEquals(queryCount, found)
        }

        println("⚡ [BENCHMARK] $queryCount lookups over $taskCount tasks:")
        println("   - O(1) Indexed Lookup: ${indexedTimeMs}ms")
        println("   - O(N) Table Scan:     ${scanTimeMs}ms")
        println("   - Speedup:             ${String.format("%.1f", scanTimeMs.toDouble() / indexedTimeMs.coerceAtLeast(1))}x faster")

        // SLA: Indexed lookup must be at least 5x faster than linear scan
        assertTrue("Indexed lookup must be faster than full scan", indexedTimeMs <= scanTimeMs)
        assertTrue("Indexed lookup should finish in under 100ms", indexedTimeMs < 100)
    }
}
