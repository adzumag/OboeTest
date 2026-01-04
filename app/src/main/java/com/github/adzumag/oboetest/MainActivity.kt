package com.github.adzumag.oboetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.adzumag.oboetest.ui.theme.OboeTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OboeTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringFromJNI(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }

    private external fun stringFromJNI(): String

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