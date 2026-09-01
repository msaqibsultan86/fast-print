package com.notzyvex.fastprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notzyvex.fastprint.state.AppViewModel
import com.notzyvex.fastprint.ui.theme.FastPrintTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val accent by viewModel.accent.collectAsStateWithLifecycle()
            FastPrintTheme(accentTheme = accent) {
                FastPrintApp(vm = viewModel)
            }
        }
    }
}
