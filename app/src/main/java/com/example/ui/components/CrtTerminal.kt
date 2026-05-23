package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatMessage
import com.example.ui.ChatSender
import kotlinx.coroutines.delay

// Supported monochrome retro-cathode-ray phosphor themes
enum class TerminalThemeColor(
    val bright: Color,
    val mid: Color,
    val dark: Color,
    val background: Color,
    val displayName: String
) {
    PHOSPHOR_GREEN(
        bright = Color(0xFF00FF33),
        mid = Color(0xFF00CC22),
        dark = Color(0xFF003308),
        background = Color(0xFF030D03),
        displayName = "PHOSPHOR GREEN (P1)"
    ),
    AMBER_YELLOW(
        bright = Color(0xFFFFB000),
        mid = Color(0xFFD48800),
        dark = Color(0xFF331C00),
        background = Color(0xFF0D0600),
        displayName = "AMBER TUBE (P3)"
    ),
    WHITE_PHOSPHOR(
        bright = Color(0xFFE5E5E5),
        mid = Color(0xFFB3B3B3),
        dark = Color(0xFF262626),
        background = Color(0xFF0F0F0F),
        displayName = "WHITE CRT (P4)"
    );

    companion object {
        fun fromIndex(index: Int): TerminalThemeColor {
            val vals = values()
            return if (index in vals.indices) vals[index] else PHOSPHOR_GREEN
        }
    }
}

// Custom drawing modifier for scanlines, curved tube border, and screen vignette glow.
fun Modifier.crtScreenOverlay(theme: TerminalThemeColor): Modifier = this.drawWithContent {
    // Draw base screen contents first
    drawContent()

    // 1. Draw horizontal phosphor television scanlines
    val scanlineSpacing = 12f
    val scanlineColor = Color.Black.copy(alpha = 0.22f)
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = scanlineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 3f
        )
        y += scanlineSpacing
    }

    // 2. Draw CRT glass screen corner vignette / radial dark shadow
    val vignetteBrush = Brush.radialGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.40f)
        ),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.minDimension * 0.9f
    )
    drawRect(brush = vignetteBrush)

    // 3. Draw subtle center cathode ray glow
    val centerGlow = Brush.radialGradient(
        colors = listOf(
            theme.bright.copy(alpha = 0.04f),
            Color.Transparent
        ),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.width * 0.7f
    )
    drawRect(brush = centerGlow)
}

@Composable
fun BlinkingCursor(theme: TerminalThemeColor) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_opacity"
    )
    Text(
        text = "█",
        color = theme.bright,
        fontFamily = FontFamily.Monospace,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.drawWithContent {
            if (alpha > 0.5f) drawContent()
        }
    )
}

@Composable
fun TypewriterText(
    text: String,
    theme: TerminalThemeColor,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
    onTypingComplete: () -> Unit = {}
) {
    var displayedChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        displayedChars = 0
        while (displayedChars < text.length) {
            delay(12) // Teletype click & feed timing speed
            displayedChars++
        }
        onTypingComplete()
    }

    Text(
        text = text.substring(0, displayedChars),
        fontFamily = FontFamily.Monospace,
        color = textColor ?: theme.bright,
        fontSize = 15.sp,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

@Composable
fun BootScreen(
    bootLogs: List<String>,
    isBooting: Boolean,
    theme: TerminalThemeColor,
    onBootClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .crtScreenOverlay(theme)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.Start
        ) {
            // Simulated System Title Frame with retro borders
            Text(
                text = "┌─────────────────────────────────────┐",
                fontFamily = FontFamily.Monospace,
                color = theme.mid,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "│          elizachatbot.com           │",
                fontFamily = FontFamily.Monospace,
                color = theme.bright,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "│      ADVANCED 1966 MAINFRAME AI     │",
                fontFamily = FontFamily.Monospace,
                color = theme.mid,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "└─────────────────────────────────────┘",
                fontFamily = FontFamily.Monospace,
                color = theme.mid,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Boot details log window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(BorderStroke(1.5.dp, theme.mid), RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(bootLogs) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            color = theme.mid,
                            fontSize = 13.sp
                        )
                    }
                    if (isBooting) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MOUNTING CORE SYNAPSES... ",
                                    fontFamily = FontFamily.Monospace,
                                    color = theme.mid,
                                    fontSize = 13.sp
                                )
                                BlinkingCursor(theme)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulse Boot Button
            if (!isBooting && bootLogs.isEmpty()) {
                Button(
                    onClick = onBootClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "Boot up administrative therapeutic console" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.bright.copy(alpha = 0.12f),
                        contentColor = theme.bright
                    ),
                    border = BorderStroke(2.dp, theme.bright),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "I N I T I A L I Z E   E L I Z A . S Y S",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    currentInput: String,
    isGenerating: Boolean,
    isOfflineMode: Boolean,
    theme: TerminalThemeColor,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onResetTerminal: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // AutoScroll to bottom when new messages appear
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background),
        containerColor = theme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(BorderStroke(1.5.dp, theme.dark), RoundedCornerShape(2.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = RoundedCornerShape(50f),
                            color = if (isOfflineMode) theme.bright.copy(alpha = 0.5f) else theme.bright
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOfflineMode) "ELIZACHATBOT.COM • LOCAL" else "ELIZACHATBOT.COM • ONLINE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = theme.bright,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "TUBE: ${theme.displayName} • SECURE PROTOCOL ACTIVE",
                        fontFamily = FontFamily.Monospace,
                        color = theme.mid,
                        fontSize = 10.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Theme toggles
                    IconButton(
                        onClick = onToggleTheme,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = theme.bright
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(BorderStroke(1.dp, theme.bright), RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Switch phosphor color scheme",
                            tint = theme.bright
                        )
                    }

                    // Flush session records
                    IconButton(
                        onClick = onResetTerminal,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = theme.bright
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(BorderStroke(1.dp, theme.bright), RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Burn logs and reset teletype clinical session",
                            tint = theme.bright
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Typing Form / Command Prompt Input Window
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(12.dp)
            ) {
                // Command-line field borders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.5.dp, theme.bright), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ",
                        fontFamily = FontFamily.Monospace,
                        color = theme.bright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    BasicTextField(
                        value = currentInput,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = theme.bright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Terminal command-line input field" },
                        cursorBrush = SolidColor(theme.bright),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (currentInput.isNotEmpty()) {
                                    onSendMessage()
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (currentInput.isEmpty()) {
                                    Text(
                                        text = "TALK NATIVELY NATIVE LANGUAGE...",
                                        fontFamily = FontFamily.Monospace,
                                        color = theme.dark,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (currentInput.isNotEmpty()) {
                                onSendMessage()
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .semantics { contentDescription = "Send inputs to ELIZA teletype" },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = theme.bright
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Submit input line",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.background)
                .crtScreenOverlay(theme)
                .padding(16.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header frame
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "┌──────────────────────────────────────┐",
                            fontFamily = FontFamily.Monospace,
                            color = theme.mid,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "│           elizachatbot.com           │",
                            fontFamily = FontFamily.Monospace,
                            color = theme.bright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "│      ADVANCED 1966 MAINFRAME AI      │",
                            fontFamily = FontFamily.Monospace,
                            color = theme.mid,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "└──────────────────────────────────────┘",
                            fontFamily = FontFamily.Monospace,
                            color = theme.mid,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "TELEPRINTER LINK • FULL RESOLUTION TELETYPE FEED ACTIVE\nSYSTEM RECEPTIVE IN ENGLISH, ESPAÑOL, DEUTSCH, FRANÇAIS & العربية",
                            fontFamily = FontFamily.Monospace,
                            color = theme.dark,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                items(messages) { message ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    if (message.sender == ChatSender.USER) theme.dark else theme.mid.copy(alpha = 0.3f)
                                ),
                                RoundedCornerShape(2.dp)
                            )
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = when (message.sender) {
                                ChatSender.USER -> "* YOU:"
                                ChatSender.ELIZA -> "* ELIZA:"
                                ChatSender.SYSTEM -> "* SYSTEM REGISTRY:"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when (message.sender) {
                                ChatSender.USER -> theme.mid
                                ChatSender.ELIZA -> theme.bright
                                ChatSender.SYSTEM -> theme.bright.copy(alpha = 0.7f)
                            },
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (message.sender == ChatSender.USER || message.sender == ChatSender.SYSTEM || message.isFullyTyped) {
                            Text(
                                text = message.text,
                                fontFamily = FontFamily.Monospace,
                                color = when (message.sender) {
                                    ChatSender.USER -> theme.mid
                                    ChatSender.ELIZA -> theme.bright
                                    ChatSender.SYSTEM -> theme.bright.copy(alpha = 0.7f)
                                },
                                fontSize = 15.sp,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            TypewriterText(
                                text = message.text,
                                theme = theme,
                                textColor = theme.bright,
                                onTypingComplete = {
                                    message.isFullyTyped = true
                                }
                            )
                        }
                    }
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(BorderStroke(0.5.dp, theme.dark), RoundedCornerShape(2.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "* MAIN SYMPTOM TRACE COMPUTING ",
                                fontFamily = FontFamily.Monospace,
                                color = theme.bright,
                                fontSize = 13.sp
                            )
                            BlinkingCursor(theme)
                        }
                    }
                }
            }
        }
    }
}
