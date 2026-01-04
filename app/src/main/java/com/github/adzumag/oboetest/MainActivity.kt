package com.github.adzumag.oboetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.github.adzumag.oboetest.ui.theme.OboeTestTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val timingAnalyzer = TimingAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // AudioEngineを起動
        startAudioEngine()

        setContent {
            OboeTestTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun TimingResultCard(result: TimingAnalyzer.TimingResult) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "測定結果",
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider()
                Text("総ビート数: ${result.totalBeats}")
                Text("平均誤差: %.2f ms".format(result.averageErrorMs))
                Text("最大誤差: %.2f ms".format(result.maxErrorMs))
                Text("最小誤差: %.2f ms".format(result.minErrorMs))
                Text("成功率 (±5ms): %.1f%%".format(result.successRate))

                Spacer(modifier = Modifier.height(8.dp))

                val status = when {
                    kotlin.math.abs(result.averageErrorMs) <= 5.0 -> "✅ 合格（±5ms以内）"
                    kotlin.math.abs(result.averageErrorMs) <= 10.0 -> "⚠️ 要調整（±10ms以内）"
                    else -> "❌ 不合格（±10ms超過）"
                }
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleSmall,
                    color = when {
                        kotlin.math.abs(result.averageErrorMs) <= 5.0 -> MaterialTheme.colorScheme.primary
                        kotlin.math.abs(result.averageErrorMs) <= 10.0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }

    @Composable
    fun MainScreen() {
        var v2Progress by remember { mutableStateOf("") }
        var v2Result by remember { mutableStateOf<TimingAnalyzer.TimingResult?>(null) }
        var v2IsRunning by remember { mutableStateOf(false) }

        var v4Progress by remember { mutableStateOf("") }
        var v4Result by remember { mutableStateOf<TimingAnalyzer.TimingResult?>(null) }
        var v4IsRunning by remember { mutableStateOf(false) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "V0: ライブラリ読み込み",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringFromJNI(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "V1: 単一音テスト",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { playTone(440f, 0.1f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("440Hz\n0.1秒", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { playTone(440f, 0.5f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("440Hz\n0.5秒", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { playTone(880f, 0.1f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("880Hz\n0.1秒", style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "V2: タイミング精度測定",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        v2IsRunning = true
                        v2Progress = "測定開始..."
                        v2Result = null

                        lifecycleScope.launch {
                            val measuredResult = timingAnalyzer.measureMetronomeTiming(
                                bpm = 120,
                                totalBeats = 100,
                                onBeat = { beat ->
                                    playTone(880f, 0.05f)
                                },
                                onProgress = { current, total ->
                                    v2Progress = "測定中: $current / $total"
                                }
                            )
                            v2Result = measuredResult
                            v2Progress = "測定完了"
                            v2IsRunning = false
                        }
                    },
                    enabled = !v2IsRunning,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("BPM 120で100回測定")
                }

                if (v2Progress.isNotEmpty()) {
                    Text(
                        text = v2Progress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (v2IsRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }

                v2Result?.let { res ->
                    TimingResultCard(result = res)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "V3: 高速テンポ・同時再生",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        lifecycleScope.launch {
                            // BPM 240での16分音符 = 62.5ms間隔
                            repeat(32) {
                                playTone(1000f, 0.03f)
                                kotlinx.coroutines.delay(62)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("BPM 240 高速連打")
                }

                Button(
                    onClick = {
                        // 5音同時再生（和音）
                        playTone(261.63f, 0.5f) // C4
                        playTone(329.63f, 0.5f) // E4
                        playTone(392.00f, 0.5f) // G4
                        playTone(523.25f, 0.5f) // C5
                        playTone(659.25f, 0.5f) // E5
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("5音同時再生テスト")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "V4: 3連符タイミング検証",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                // 4分音符3連符: BPM 120 → 333.33ms間隔
                                val interval = timingAnalyzer.calculateTripletInterval(120, "quarter")
                                repeat(9) { // 3拍分（3×3=9音）
                                    playTone(800f, 0.05f)
                                    kotlinx.coroutines.delay(interval.toLong())
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("4分3連\n再生", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                // 8分音符3連符: BPM 120 → 166.67ms間隔
                                val interval = timingAnalyzer.calculateTripletInterval(120, "eighth")
                                repeat(12) { // 2拍分（2×6=12音）
                                    playTone(1200f, 0.03f)
                                    kotlinx.coroutines.delay(interval.toLong())
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("8分3連\n再生", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Button(
                    onClick = {
                        v4IsRunning = true
                        v4Progress = "3連符測定開始..."
                        v4Result = null

                        lifecycleScope.launch {
                            val measuredResult = timingAnalyzer.measureTripletTiming(
                                bpm = 120,
                                tripletType = "eighth",
                                totalTriplets = 90, // 30セット（90音）
                                onBeat = { beat ->
                                    playTone(1200f, 0.03f)
                                },
                                onProgress = { current, total ->
                                    v4Progress = "3連符測定中: $current / $total"
                                }
                            )
                            v4Result = measuredResult
                            v4Progress = "3連符測定完了"
                            v4IsRunning = false
                        }
                    },
                    enabled = !v4IsRunning,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("8分3連で90回測定")
                }

                if (v4Progress.isNotEmpty()) {
                    Text(
                        text = v4Progress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (v4IsRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }

                v4Result?.let { res ->
                    TimingResultCard(result = res)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "V5: ADSRエンベロープ",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { playTone(440f, 0.2f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("通常\n440Hz", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { playToneWithADSR(440f, 0.2f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ADSR\n440Hz", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // 通常音の和音
                            playTone(261.63f, 0.5f) // C4
                            playTone(329.63f, 0.5f) // E4
                            playTone(392.00f, 0.5f) // G4
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("通常\n和音", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            // ADSRエンベロープ適用の和音
                            playToneWithADSR(261.63f, 0.5f) // C4
                            playToneWithADSR(329.63f, 0.5f) // E4
                            playToneWithADSR(392.00f, 0.5f) // G4
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ADSR\n和音", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioEngine()
    }

    private external fun stringFromJNI(): String
    private external fun startAudioEngine(): Boolean
    private external fun stopAudioEngine()
    private external fun playTone(frequency: Float, duration: Float)
    private external fun playToneWithADSR(frequency: Float, duration: Float)

    companion object {
        init {
            System.loadLibrary("oboetest")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    OboeTestTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "V0: ライブラリ読み込み",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "✓ Oboe読み込み成功",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}