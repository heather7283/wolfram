package io.github.heather7283.wolfram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.heather7283.wolfram.ui.WolframNavHost
import io.github.heather7283.wolfram.ui.theme.WolframTheme

@AndroidEntryPoint
class WolframActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WolframTheme {
                WolframNavHost()
            }
        }
    }
}
