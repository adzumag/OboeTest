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
    fun MainScreen() {
        var progress by remember { mutableStateOf("") }
        var result by remember { mutableStateOf<TimingAnalyzer.TimingResult?>(null) }
        var isRunning by remember { mutableStateOf(false) }

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
                    text = stringFromJNI(),
                    style = MaterialTheme.typography.headlineSmall
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "単一音テスト",
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

                Divider(modifier = Modifier.padding(vertical = 8.dp))

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

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "V2: タイミング精度測定",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        isRunning = true
                        progress = "測定開始..."
                        result = null

                        lifecycleScope.launch {
                            val measuredResult = timingAnalyzer.measureMetronomeTiming(
                                bpm = 120,
                                totalBeats = 100,
                                onBeat = { beat ->
                                    playTone(880f, 0.05f)
                                },
                                onProgress = { current, total ->
                                    progress = "測定中: $current / $total"
                                }
                            )
                            result = measuredResult
                            progress = "測定完了"
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("BPM 120で100回測定")
                }

                if (progress.isNotEmpty()) {
                    Text(
                        text = progress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }

                result?.let { res ->
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
                            Divider()
                            Text("総ビート数: ${res.totalBeats}")
                            Text("平均誤差: %.2f ms".format(res.averageErrorMs))
                            Text("最大誤差: %.2f ms".format(res.maxErrorMs))
                            Text("最小誤差: %.2f ms".format(res.minErrorMs))
                            Text("成功率 (±5ms): %.1f%%".format(res.successRate))

                            Spacer(modifier = Modifier.height(8.dp))

                            val status = when {
                                kotlin.math.abs(res.averageErrorMs) <= 5.0 -> "✅ 合格（±5ms以内）"
                                kotlin.math.abs(res.averageErrorMs) <= 10.0 -> "⚠️ 要調整（±10ms以内）"
                                else -> "❌ 不合格（±10ms超過）"
                            }
                            Text(
                                text = status,
                                style = MaterialTheme.typography.titleSmall,
                                color = when {
                                    kotlin.math.abs(res.averageErrorMs) <= 5.0 -> MaterialTheme.colorScheme.primary
                                    kotlin.math.abs(res.averageErrorMs) <= 10.0 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
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

    companion object {
        init {
            System.loadLibrary("oboetest")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OboeTestTheme {
        Text("Oboe version: 1.9.0")
    }
}