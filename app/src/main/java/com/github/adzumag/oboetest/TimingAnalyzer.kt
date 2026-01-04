package com.github.adzumag.oboetest

import android.util.Log
import kotlinx.coroutines.delay

class TimingAnalyzer {
    data class TimingResult(
        val totalBeats: Int,
        val averageErrorMs: Double,
        val maxErrorMs: Double,
        val minErrorMs: Double,
        val successRate: Double
    )

    suspend fun measureMetronomeTiming(
        bpm: Int,
        totalBeats: Int,
        onBeat: (Int) -> Unit,
        onProgress: (Int, Int) -> Unit
    ): TimingResult {
        val intervalMs = (60000.0 / bpm).toLong()
        val errors = mutableListOf<Double>()

        Log.i("TimingAnalyzer", "Starting metronome: BPM=$bpm, interval=${intervalMs}ms, beats=$totalBeats")

        for (beat in 0 until totalBeats) {
            val expectedTime = System.nanoTime()

            onBeat(beat)
            onProgress(beat + 1, totalBeats)

            val afterPlayTime = System.nanoTime()
            val elapsedMs = (afterPlayTime - expectedTime) / 1_000_000.0

            // 次のビートまでの待機時間を計算
            val waitTime = intervalMs - elapsedMs.toLong()
            if (waitTime > 0) {
                delay(waitTime)
            }

            val actualTime = System.nanoTime()
            val actualIntervalMs = if (beat > 0) {
                (actualTime - expectedTime) / 1_000_000.0
            } else {
                intervalMs.toDouble()
            }

            val errorMs = actualIntervalMs - intervalMs
            errors.add(errorMs)

            Log.d("TimingAnalyzer", "Beat $beat: expected=${intervalMs}ms, actual=${actualIntervalMs}ms, error=${errorMs}ms")
        }

        val avgError = errors.average()
        val maxError = errors.maxOrNull() ?: 0.0
        val minError = errors.minOrNull() ?: 0.0
        val successCount = errors.count { kotlin.math.abs(it) <= 5.0 }
        val successRate = (successCount.toDouble() / errors.size) * 100

        return TimingResult(
            totalBeats = totalBeats,
            averageErrorMs = avgError,
            maxErrorMs = maxError,
            minErrorMs = minError,
            successRate = successRate
        )
    }
}
