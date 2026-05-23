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

// Colors matching original phosphorus-phosphor terminals (P1 / P3 tubes)
val RetroBlack = Color(0xFF030A02)
val PhosphorGreenBright = Color(0xFF00FF33)
val PhosphorGreenMid = Color(0xFF00CC22)
val PhosphorGreenDark = Color(0xFF003308)
val AmberYellow = Color(0xFFFFB000)

// Custom drawing modifier for scanlines, curved tube border, and screen vignette glow.
fun Modifier.crtScreenOverlay(): Modifier = this.drawWithContent {
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
            Color(0x0E00FF33),
            Color.Transparent
        ),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.width * 0.7f
    )
    drawRect(brush = centerGlow)
}

@Composable
fun BlinkingCursor() {
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
        color = PhosphorGreenBright,
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
    modifier: Modifier = Modifier,
    textColor: Color = PhosphorGreenBright,
    onTypingComplete: () -> Unit = {}
) {
    var displayedChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        displayedChars = 0
        while (displayedChars < text.length) {
            delay(15) // Teletype click & feed timing speed
            displayedChars++
        }
        onTypingComplete()
    }

    Text(
        text = text.substring(0, displayedChars),
        fontFamily = FontFamily.Monospace,
        color = textColor,
        fontSize = 15.sp,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

@Composable
fun BootScreen(
    bootLogs: List<String>,
    isBooting: Boolean,
    onBootClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroBlack)
            .crtScreenOverlay()
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
            // Simulated System Title Frame
            Text(
                text = "=====================================",
                fontFamily = FontFamily.Monospace,
                color = PhosphorGreenBright,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "  W E I Z E N B A U M   T E R M I N A L  ",
                fontFamily = FontFamily.Monospace,
                color = PhosphorGreenBright,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "  M O D E L   3 3 - L T D   1 9 6 6  ",
                fontFamily = FontFamily.Monospace,
                color = PhosphorGreenBright,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "=====================================",
                fontFamily = FontFamily.Monospace,
                color = PhosphorGreenBright,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Boot details log window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(BorderStroke(1.dp, PhosphorGreenMid), RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(bootLogs) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenMid,
                            fontSize = 13.sp
                        )
                    }
                    if (isBooting) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MOUNTING RETRO BUFFER ",
                                    fontFamily = FontFamily.Monospace,
                                    color = PhosphorGreenMid,
                                    fontSize = 13.sp
                                )
                                BlinkingCursor()
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
                        .semantics { contentDescription = "Boot up therapeutic console" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhosphorGreenBright.copy(alpha = 0.15f),
                        contentColor = PhosphorGreenBright
                    ),
                    border = BorderStroke(2.dp, PhosphorGreenBright),
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
    customApiKey: String,
    onCustomApiKeyChange: (String) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onResetTerminal: () -> Unit
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showKeyDialog by remember { mutableStateOf(false) }

    // AutoScroll to bottom when new messages appear
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showKeyDialog) {
        Dialog(
            onDismissRequest = { showKeyDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .border(BorderStroke(2.dp, PhosphorGreenBright), RoundedCornerShape(8.dp))
                    .background(RetroBlack)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "+---------------------------------------+",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenBright,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "|      API ACCESS SYSTEM CONTROL        |",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "+---------------------------------------+",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenBright,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "THE CHATBOT SENDS PROMPTS TO GOOGLE GEMINI API.\n" +
                               "IF THE CONNECTION IS OFFLINE OR RETURNS 403, YOU CAN ENTER YOUR PRIVATE API KEY OR DEFINE IT SECURELY IN GOOGLE AI STUDIO SECRETS.",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenMid,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "توجيهات فك التشفير:\n" +
                               "إذا واجهت مشكلة في الاتصال (مثل خطأ 403)، فهذا يعني أن مفتاح النظام المدمج غير متاح حالياً. يمكنك لصق مفتاح Gemini API الخاص بك بالأسفل وتفعيله لحل المشكلة فوراً.",
                        fontFamily = FontFamily.Monospace,
                        color = AmberYellow,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "INJECT DECRYPTER KEY (API KEY):",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    var tempKey by remember { mutableStateOf(customApiKey) }
                    BasicTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenBright,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(PhosphorGreenBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, PhosphorGreenBright), RoundedCornerShape(4.dp))
                            .background(Color.Black)
                            .padding(12.dp),
                        decorationBox = { innerTextField ->
                            if (tempKey.isEmpty()) {
                                Text(
                                    text = "PASTE AI_STUDIO_API_KEY HERE...",
                                    fontFamily = FontFamily.Monospace,
                                    color = PhosphorGreenDark,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showKeyDialog = false }
                        ) {
                            Text(
                                "ABORT",
                                fontFamily = FontFamily.Monospace,
                                color = PhosphorGreenMid,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onCustomApiKeyChange(tempKey)
                                showKeyDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PhosphorGreenBright.copy(alpha = 0.15f),
                                contentColor = PhosphorGreenBright
                            ),
                            border = BorderStroke(1.dp, PhosphorGreenBright),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "SAVE & COMMIT",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroBlack),
        containerColor = RetroBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(BorderStroke(1.dp, PhosphorGreenDark), RoundedCornerShape(2.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(8.dp),
                            shape = RoundedCornerShape(50f),
                            color = if (isOfflineMode) AmberYellow else PhosphorGreenBright
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOfflineMode) "ELIZA: LOCAL ENGINE (1966)" else "ELIZA: GEMINI HYBRID TELETYPE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isOfflineMode) AmberYellow else PhosphorGreenBright,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "SESSION ACTIVE • ALL CHANNELS COMPILING",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenMid,
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // API Key Settings Button
                    IconButton(
                        onClick = { showKeyDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = PhosphorGreenBright
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(BorderStroke(1.dp, PhosphorGreenBright), RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure manual API key",
                            tint = PhosphorGreenBright
                        )
                    }

                    // Burn Records Button
                    IconButton(
                        onClick = onResetTerminal,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = PhosphorGreenBright
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .border(BorderStroke(1.dp, PhosphorGreenBright), RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Burn logs and reset clinical session",
                            tint = PhosphorGreenBright
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
                    .background(Color.Black.copy(alpha = 0.9f))
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(12.dp)
            ) {
                if (isOfflineMode) {
                    // Inline disclaimer for offline simulation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Offline indicator info",
                            tint = AmberYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SECURE LOCAL SANDBOX ACTIVE. DEMO CONVERSATION LOCAL.",
                            fontFamily = FontFamily.Monospace,
                            color = AmberYellow,
                            fontSize = 9.sp
                        )
                    }
                }

                // Command-line field borders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.5.dp, PhosphorGreenBright), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ",
                        fontFamily = FontFamily.Monospace,
                        color = PhosphorGreenBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    BasicTextField(
                        value = currentInput,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Terminal command-line input field" },
                        cursorBrush = SolidColor(PhosphorGreenBright),
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
                                        text = "INPUT SYMPTOMS HERE...",
                                        fontFamily = FontFamily.Monospace,
                                        color = PhosphorGreenDark,
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
                            contentColor = PhosphorGreenBright
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
                .background(RetroBlack)
                .crtScreenOverlay()
                .padding(16.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ASCII Teletype session frame
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "+---------------------------------------+",
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenMid,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "|  ELIZA PSYCHOTHERAPIST CORE SYSTEM    |",
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenMid,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "|  MAY 1966 MAINFRAME DIRECT CONNECTOR  |",
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenMid,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "+---------------------------------------+",
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenMid,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "ESTABLISHING TELEPRINTER LINK...\nTELETYPE ASR-33 FEED CONNECTED.",
                            fontFamily = FontFamily.Monospace,
                            color = PhosphorGreenDark,
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
                                    if (message.sender == ChatSender.USER) PhosphorGreenDark else PhosphorGreenMid.copy(alpha = 0.3f)
                                ),
                                RoundedCornerShape(2.dp)
                            )
                            .background(Color.Black.copy(alpha = 0.5f))
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
                                ChatSender.USER -> PhosphorGreenMid
                                ChatSender.ELIZA -> PhosphorGreenBright
                                ChatSender.SYSTEM -> AmberYellow
                            },
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (message.sender == ChatSender.USER || message.sender == ChatSender.SYSTEM || message.isFullyTyped) {
                            Text(
                                text = message.text,
                                fontFamily = FontFamily.Monospace,
                                color = when (message.sender) {
                                    ChatSender.USER -> PhosphorGreenMid
                                    ChatSender.ELIZA -> PhosphorGreenBright
                                    ChatSender.SYSTEM -> AmberYellow
                                },
                                fontSize = 15.sp,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            TypewriterText(
                                text = message.text,
                                textColor = PhosphorGreenBright,
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
                                .border(BorderStroke(0.5.dp, PhosphorGreenDark), RoundedCornerShape(2.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "* ELIZA STATUS: CALCULATING RESPONSE TRACE ",
                                fontFamily = FontFamily.Monospace,
                                color = PhosphorGreenBright,
                                fontSize = 13.sp
                            )
                            BlinkingCursor()
                        }
                    }
                }
            }
        }
    }
}
