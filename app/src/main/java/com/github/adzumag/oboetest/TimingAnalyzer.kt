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

    /**
     * 3連符のタイミング間隔を計算
     * @param bpm テンポ
     * @param tripletType "quarter" (4分音符3連) or "eighth" (8分音符3連)
     * @return 間隔（ミリ秒）
     */
    fun calculateTripletInterval(bpm: Int, tripletType: String): Double {
        return when (tripletType) {
            "quarter" -> (60000.0 / bpm) * (2.0 / 3.0) // 4分音符3連
            "eighth" -> (60000.0 / bpm) * (1.0 / 3.0)  // 8分音符3連
            else -> throw IllegalArgumentException("Invalid triplet type: $tripletType")
        }
    }

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

    /**
     * 3連符のタイミング精度を測定
     */
    suspend fun measureTripletTiming(
        bpm: Int,
        tripletType: String,
        totalTriplets: Int,
        onBeat: (Int) -> Unit,
        onProgress: (Int, Int) -> Unit
    ): TimingResult {
        val intervalMs = calculateTripletInterval(bpm, tripletType)
        val errors = mutableListOf<Double>()

        Log.i("TimingAnalyzer", "Starting triplet: BPM=$bpm, type=$tripletType, interval=${intervalMs}ms, triplets=$totalTriplets")

        var lastTime = System.nanoTime()

        for (beat in 0 until totalTriplets) {
            val expectedTime = System.nanoTime()

            onBeat(beat)
            onProgress(beat + 1, totalTriplets)

            val afterPlayTime = System.nanoTime()
            val elapsedMs = (afterPlayTime - expectedTime) / 1_000_000.0

            // 次のビートまでの待機時間を計算
            val waitTime = intervalMs - elapsedMs
            if (waitTime > 0) {
                delay(waitTime.toLong())
            }

            val actualTime = System.nanoTime()
            val actualIntervalMs = if (beat > 0) {
                (actualTime - lastTime) / 1_000_000.0
            } else {
                intervalMs
            }

            lastTime = actualTime

            val errorMs = actualIntervalMs - intervalMs
            errors.add(errorMs)

            Log.d("TimingAnalyzer", "Triplet $beat: expected=${intervalMs}ms, actual=${actualIntervalMs}ms, error=${errorMs}ms")
        }

        val avgError = errors.average()
        val maxError = errors.maxOrNull() ?: 0.0
        val minError = errors.minOrNull() ?: 0.0
        val successCount = errors.count { kotlin.math.abs(it) <= 5.0 }
        val successRate = (successCount.toDouble() / errors.size) * 100

        return TimingResult(
            totalBeats = totalTriplets,
            averageErrorMs = avgError,
            maxErrorMs = maxError,
            minErrorMs = minError,
            successRate = successRate
        )
    }
}
