package com.andrespar2.parfmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.andrespar2.parfmi.ui.ParFmiNavHost
import com.andrespar2.parfmi.ui.theme.ParfmiColors
import com.andrespar2.parfmi.ui.theme.ParFmiTheme

/** Unica actividad de la app; aloja el grafo de navegacion Compose. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ParFmiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ParfmiColors.AmoledBlack,
                ) {
                    ParFmiNavHost()
                }
            }
        }
    }
}
