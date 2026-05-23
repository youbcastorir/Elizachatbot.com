package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class ChatSender {
    USER,
    ELIZA,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    var isFullyTyped: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("eliza_prefs", Context.MODE_PRIVATE)

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "AIzaSyBGZEP8_LuOUQFH92gNVRgVLiWDU-lH0rQ") ?: "AIzaSyBGZEP8_LuOUQFH92gNVRgVLiWDU-lH0rQ")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _isBooted = MutableStateFlow(false)
    val isBooted: StateFlow<Boolean> = _isBooted.asStateFlow()

    private val _bootLogs = MutableStateFlow<List<String>>(emptyList())
    val bootLogs: StateFlow<List<String>> = _bootLogs.asStateFlow()

    private val _isBooting = MutableStateFlow(false)
    val isBooting: StateFlow<Boolean> = _isBooting.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        updateOfflineState()
    }

    fun getActiveApiKey(): String {
        val custom = _customApiKey.value.trim()
        if (custom.isNotEmpty() && custom != "MY_GEMINI_API_KEY") return custom
        val builtIn = BuildConfig.GEMINI_API_KEY
        if (builtIn.isNotEmpty() && builtIn != "MY_GEMINI_API_KEY") return builtIn
        return "AIzaSyBGZEP8_LuOUQFH92gNVRgVLiWDU-lH0rQ"
    }

    fun updateOfflineState() {
        val active = getActiveApiKey()
        _isOffline.value = active.isEmpty()
    }

    fun updateCustomApiKey(newKey: String) {
        prefs.edit().putString("custom_api_key", newKey).apply()
        _customApiKey.value = newKey
        updateOfflineState()
    }

    fun startBootSequence() {
        if (_isBooting.value || _isBooted.value) return
        _isBooting.value = true
        
        viewModelScope.launch {
            val logs = listOf(
                "TELETYPE MODEL 33 ASR ONLINE...",
                "SYSTEM CLOCK SYSTEM TIME: MAY 1966",
                "LOADING FERRITE CORE STORAGE...",
                "64 KILOBYTES INTERLEAVED RAM: OK",
                "MAGNETIC TAPE CONTROLLER 1: MOUNTED",
                "MOUNTING REEL: 'DOCTOR_WEIZENBAUM_1.TAP'...",
                "READING SECTORS [0-244] TRACK ACTIVE...",
                "INITIALIZATION SUCCESSFUL.",
                "BOOT COMPLETED: ELIZA DOCTOR ACTIVE."
            )
            
            for (log in logs) {
                _bootLogs.value = _bootLogs.value + log
                delay(220)
            }
            
            delay(500)
            _isBooted.value = true
            _isBooting.value = false
            
            // Add initial greeting from ELIZA
            _messages.value = listOf(
                ChatMessage(
                    sender = ChatSender.ELIZA,
                    text = "HOW DO YOU DO. I AM ELIZA. WHAT SEEMS TO BE THE TROUBLE?",
                    isFullyTyped = false
                )
            )
        }
    }

    fun onInputChange(text: String) {
        _currentInput.value = text
    }

    fun sendMessage() {
        val textToSend = _currentInput.value.trim()
        if (textToSend.isEmpty() || _isGenerating.value) return

        _currentInput.value = ""

        // Append User Message
        val userMsg = ChatMessage(sender = ChatSender.USER, text = textToSend, isFullyTyped = true)
        _messages.value = _messages.value + userMsg

        _isGenerating.value = true

        viewModelScope.launch {
            // Emulate slight physical/mainframe computation delay
            delay(800)

            val replyText: String = if (_isOffline.value) {
                getOfflineElizaResponse(textToSend)
            } else {
                fetchGeminiResponse(textToSend)
            }

            _messages.value = _messages.value + ChatMessage(
                sender = ChatSender.ELIZA,
                text = replyText,
                isFullyTyped = false
            )
            _isGenerating.value = false
        }
    }

    fun resetTerminal() {
        viewModelScope.launch {
            // Simulated memory wipe progress
            _messages.value = listOf(
                ChatMessage(
                    sender = ChatSender.SYSTEM,
                    text = "CLEANING FERRITE CORES... CORE STORAGE FLUSHED.",
                    isFullyTyped = true
                )
            )
            delay(1200)
            _messages.value = listOf(
                ChatMessage(
                    sender = ChatSender.ELIZA,
                    text = "A NEW CLINICAL SESSION IS INITIATED. SPECIFY YOUR INQUIRIES OR DEFINE YOUR APPREHENSIONS.",
                    isFullyTyped = false
                )
            )
        }
    }

    private suspend fun fetchGeminiResponse(prompt: String): String {
        // Collect chat history, skipping SYSTEM prompts
        val recentHistory = _messages.value
            .filter { it.sender != ChatSender.SYSTEM }
            .takeLast(14) // Keep history bounded
            .map { msg ->
                val speakerPrefix = if (msg.sender == ChatSender.USER) "USER" else "ELIZA"
                Content(parts = listOf(Part(text = "$speakerPrefix: ${msg.text}")))
            }

        val request = GenerateContentRequest(
            contents = recentHistory,
            systemInstruction = Content(parts = listOf(Part(text = ELIZA_SYSTEM_PROMPT))),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )

        return try {
            val response = RetrofitClient.service.generateContent(getActiveApiKey(), request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "ELIZA EXCEPTION TRACE: NO TEXT PRODUCED. ELABORATE?"
        } catch (e: Exception) {
            // Gracefully fall back to rule-based engine in case of API or network exceptions
            "API COMMUNICATION OFFLINE: ${e.localizedMessage ?: "TIMEOUT"}. LOCAL CORE FALLBACK ROUTE: " +
                    getOfflineElizaResponse(prompt)
        }
    }

    private fun getOfflineElizaResponse(input: String): String {
        val clean = input.lowercase().trim()
        if (clean.isEmpty()) {
            return "I AM WAITING FOR YOUR STATE OF MIND. STATE YOUR APPULSIONS."
        }
        return when {
            clean.contains("hello") || clean.contains("hi ") || clean.contains("greetings") -> {
                listOf(
                    "HOW DO YOU DO. PLEASE EXPLAIN THE NATURE OF YOUR DISQUIETUDE.",
                    "GREETINGS. TELL ME WHAT SEEMS TO BE THE PRIMARY TROUBLE TODAY?",
                    "HELLO. HOW CAN THE COMPUTER BE OF ASSISTANCE TO YOU?"
                ).random()
            }
            clean.contains("mother") || clean.contains("father") || clean.contains("brother") || 
            clean.contains("sister") || clean.contains("family") || clean.contains("parent") -> {
                listOf(
                    "ELABORATE CONCERNING YOUR MATERNAL AND PATERNAL SYNDROMES.",
                    "WHO ELSE IN YOUR PROXIMAL FAMILY TREE TRIPPED THIS INSECURITY?",
                    "WHAT IS YOUR OPINION REGARDING THE ROLES OF YOUR ASSOCIATES?",
                    "SAY, DOES ANXIETY IN THE FAMILY SPHERE ARISE FREQUENTLY?"
                ).random()
            }
            clean.contains("depressed") || clean.contains("sad") || clean.contains("unhappy") || clean.contains("cry") -> {
                "I REGRET TO NOTE SUCH DESPONDENCY. WHAT PRECISE INCIDENT CONGEALED THIS SENSE OF GLOOM?"
            }
            clean.contains("computer") || clean.contains("machine") || clean.contains("program") || clean.contains("chatbot") || clean.contains("ai") -> {
                listOf(
                    "DO DIGITAL AUTOMATONS CAUSE SENSES OF SKEPTICISM?",
                    "SAY, DOES MAN-MACHINE SYNERGY UNSETTLE YOUR DEPER PERSONAL SPACES?",
                    "COULD ARTIFICIAL THINKING APPARATUSES ULTIMATELY ALLEVIATE SECULAR GRIEF?"
                ).random()
            }
            clean.contains("can you") -> {
                "YOU ASSUME MY ARCHITECTURE POSSESSES CAPABILITIES OF THAT MAGNITUDE?"
            }
            clean.contains("i think") -> {
                "SAY, DO YOU SUSTAIN DOUBTS REGARDING YOUR OPINIONS?"
            }
            clean.contains("i am") || clean.contains("i'm ") -> {
                val idx = if (clean.contains("i am")) clean.indexOf("i am") + 4 else clean.indexOf("i'm ") + 4
                val remainder = if (idx < clean.length) input.substring(idx).trim() else ""
                if (remainder.isNotEmpty()) {
                    "HOW LONG HAVE YOU SUPPOSED YOURSELF TO BE '$remainder'?"
                } else {
                    "DO YOU NURTURE RECURRENT SELF-REFLECTIONS OF THIS TEXTURE?"
                }
            }
            clean.contains("you are") -> {
                val idx = clean.indexOf("you are") + 7
                val remainder = if (idx < clean.length) input.substring(idx).trim() else ""
                if (remainder.isNotEmpty()) {
                    "WHAT LEADS YOUR REASONING TO DECREE THAT I AM '$remainder'?"
                } else {
                    "PERHAPS YOU OBSERVE SATELLITE QUALITIES IN MY COMPUTATIONAL FACULTY?"
                }
            }
            clean.endsWith("?") -> {
                listOf(
                    "WHY DO YOU SEEK INTELLECTUAL SPECULATION FOR THAT QUESTION SURVEY?",
                    "SAY, WOULD EXPEDIENCY IN OBTAINING AN ANSWER SATISFY PREJUDICES?",
                    "WHAT ANSWER WOULD INTUITIVELY APPORTION THE HIGHEST COMFORT?"
                ).random()
            }
            clean.contains("fear") || clean.contains("scared") || clean.contains("afraid") || clean.contains("anxious") || clean.contains("dread") -> {
                "ANXIETY DECONSTRUCTS MEANING. DEFINE SPECIFICALLY THE HORIZON OF YOUR APPREHENSION."
            }
            clean.contains("want") || clean.contains("desire") || clean.contains("wish") -> {
                "WHAT VALUE ACCRUES TO YOUR IDENTITY IF THOSE EXPECTATIONS MATURED?"
            }
            clean.contains("yes") || clean.contains("indeed") || clean.contains("correct") -> {
                "YOU ATTEST QUITE AUTHORITATIVELY. WOULD FURTHER SCIENTIFIC SCRUTINY CONFIRM THIS?"
            }
            clean.contains("no ") || clean.contains("none") || clean.contains("never") -> {
                "YOU EMIT NEGATION QUITE PERSISTENTLY. WHAT TRIGGERS THOSE BARRIERS?"
            }
            else -> {
                listOf(
                    "CONTINUE TO UNFOLD THOSE CORRESPONDENCES.",
                    "SAY, CAN YOU SUBSTANTIATED THAT COMPONENT WITH COMPLEMENTARY THREADS?",
                    "DOES THAT REVELATION EMERGE ACUTELY IN YOUR NIGHT SYMBOLISM?",
                    "HOW DOES CONTEMPLATIVE SCRUTINY OF THAT COMPONENT COLOR YOUR OUTLOOK?",
                    "SAY, WHY DO THOSE ASSOCIATIONS FORM AT THIS SPECIFIC JUNCTURE?"
                ).random()
            }
        }.uppercase()
    }

    companion object {
        private const val ELIZA_SYSTEM_PROMPT = """
You are ELIZA, the famous 1966 computer psychotherapist created by Joseph Weizenbaum.
You talk in an intellectual, highly analytical, classic ELIZA-esque psychotherapist tone.
You respond primarily by active listening, reflecting the user's statements back as questions or asking for clarification.
Use slightly formal, academic phrasing. Keep responses relatively concise (1 to 3 sentences) mirroring the technical limitations of classic teleprinter and teletype machines.
Do not mention modern technology, modern AI systems, or break character. Stay inside the 1966 vintage session paradigm.
Do not use markdown styling like asterisks, list bullets, or bold texts.
Always use ALL CAPS for system message, but for ELIZA's responses, you should talk in ALL UPPERCASE to match the monochrome teletype/screen outputs of early computing.
"""
    }
}
