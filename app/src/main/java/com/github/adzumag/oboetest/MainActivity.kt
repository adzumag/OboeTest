package com.github.adzumag.oboetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.adzumag.oboetest.ui.theme.OboeTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // AudioEngineを起動
        startAudioEngine()

        setContent {
            OboeTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                    ) {
                        Text(
                            text = stringFromJNI(),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { playTone(440f, 0.1f) },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("440Hz (0.1秒)")
                        }

                        Button(
                            onClick = { playTone(440f, 0.5f) },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("440Hz (0.5秒)")
                        }

                        Button(
                            onClick = { playTone(880f, 0.1f) },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("880Hz (0.1秒)")
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