package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ChatViewModel
import com.example.ui.components.BootScreen
import com.example.ui.components.ChatScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val viewModel: ChatViewModel = viewModel()
                val isBooted by viewModel.isBooted.collectAsStateWithLifecycle()
                val isBooting by viewModel.isBooting.collectAsStateWithLifecycle()
                val bootLogs by viewModel.bootLogs.collectAsStateWithLifecycle()
                val messages by viewModel.messages.collectAsStateWithLifecycle()
                val currentInput by viewModel.currentInput.collectAsStateWithLifecycle()
                val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
                val isOfflineMode by viewModel.isOffline.collectAsStateWithLifecycle()

                if (isBooted) {
                    ChatScreen(
                        messages = messages,
                        currentInput = currentInput,
                        isGenerating = isGenerating,
                        isOfflineMode = isOfflineMode,
                        onInputChange = { viewModel.onInputChange(it) },
                        onSendMessage = { viewModel.sendMessage() },
                        onResetTerminal = { viewModel.resetTerminal() }
                    )
                } else {
                    BootScreen(
                        bootLogs = bootLogs,
                        isBooting = isBooting,
                        onBootClick = { viewModel.startBootSequence() }
                    )
                }
            }
        }
    }
}
