package com.cactus.example.pages

import android.text.format.DateFormat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cactus.*
import com.cactus.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

enum class InputMode {
    TEXT, VOICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HybridChatPage(
    onBack: () -> Unit,
    onNavigateToMap: () -> Unit = {}  // New callback for map navigation
) {
    val scope = rememberCoroutineScope()

    // LLM state
    val lm = remember { CactusLM() }
    var chatMessages by remember { mutableStateOf<List<HybridMessage>>(emptyList()) }
    var isLmLoading by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Track if this is the first user message (for demo navigation)
    var hasUserSentMessage by remember { mutableStateOf(false) }

    // STT state
    var stt by remember { mutableStateOf<CactusSTT?>(null) }
    var isSttModelLoaded by remember { mutableStateOf(false) }
    var isSttDownloading by remember { mutableStateOf(false) }
    var isSttInitializing by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var sttStatusText by remember { mutableStateOf("Loading voice model...") }
    val selectedVoiceModel = "whisper-tiny"

    // Input mode state
    var inputMode by remember { mutableStateOf(InputMode.TEXT) }

    // Date/time state
    val context = LocalContext.current
    var currentDateTime by remember { mutableStateOf(getCurrentDateTime(context)) }

    // Auto-refresh date/time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = getCurrentDateTime(context)
            delay(1000L)
        }
    }

    // Initialize LLM on first composition
    LaunchedEffect(Unit) {
        try {
            lm.downloadModel()
            lm.initializeModel(params = CactusInitParams(model = "qwen3-0.6", contextSize = 4096))
            // Warm up with system message
            lm.generateCompletion(
                messages = listOf(
                    ChatMessage("You are Cactus, a very capable AI assistant running offline on a smartphone", "system")
                ),
                params = CactusCompletionParams(maxTokens = 0)
            )
            isLmLoading = false
        } catch (e: Exception) {
            println("Error setting up CactusLM: ${e.message}")
            isLmLoading = false
        }
    }

    // Initialize STT on first composition (in parallel with LLM)
    LaunchedEffect(Unit) {
        stt = CactusSTT(TranscriptionProvider.WHISPER)

        isSttDownloading = true
        isSttInitializing = true
        sttStatusText = "Downloading voice model..."

        try {
            val downloadSuccess = stt!!.download(model = selectedVoiceModel)

            if (!downloadSuccess) {
                isSttDownloading = false
                isSttInitializing = false
                sttStatusText = "Failed to download model"
                return@LaunchedEffect
            }

            isSttDownloading = false
            sttStatusText = "Initializing voice..."

            val initSuccess = stt!!.init(model = selectedVoiceModel)
            isSttInitializing = false

            if (initSuccess) {
                isSttModelLoaded = true
                sttStatusText = "Tap mic to speak"
            } else {
                sttStatusText = "Failed to initialize model"
            }
        } catch (e: Exception) {
            isSttDownloading = false
            isSttInitializing = false
            sttStatusText = "Error: ${e.message}"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            lm.unload()
            stt?.stop()
        }
    }

    fun sendMessage(content: String) {
        val message = content.trim()
        if (message.isEmpty()) return

        scope.launch {
            // Add user message
            chatMessages = chatMessages + HybridMessage(content = message, role = HybridMessageRole.User)
            messageText = ""

            // Mark that user has sent a message
            val isFirstMessage = !hasUserSentMessage
            hasUserSentMessage = true

            // Add typing indicator
            chatMessages = chatMessages + HybridMessage(content = "", role = HybridMessageRole.Typing)
            isGenerating = true

            // Scroll to bottom
            listState.animateScrollToItem(chatMessages.size - 1)

            // For demo: Show a hardcoded response and navigate to map
            if (isFirstMessage) {
                // Remove typing indicator after a brief delay
                delay(1500)
                chatMessages = chatMessages.filter { it.role != HybridMessageRole.Typing }

                // Add hardcoded assistant response
                val demoResponse = "Sure! I'll find the best route to the Tower of London for you. Opening the map now..."
                chatMessages = chatMessages + HybridMessage(
                    content = demoResponse,
                    role = HybridMessageRole.Assistant
                )

                isGenerating = false

                // Scroll to show the response
                listState.animateScrollToItem(chatMessages.size - 1)

                // Wait a moment so user can read the response, then navigate
                delay(2000)
                onNavigateToMap()
                return@launch
            }

            // Normal LLM flow for subsequent messages (if any)
            try {
                val assistantResponse = StringBuilder()
                val messagesToPass = chatMessages.filter { it.role != HybridMessageRole.Typing }
                    .map { ChatMessage(it.content, it.role.toApiRole()) }

                val result = lm.generateCompletion(
                    messages = messagesToPass,
                    onToken = { token, _ ->
                        // Remove typing indicator and update/add assistant message
                        chatMessages = chatMessages.filter { it.role != HybridMessageRole.Typing }

                        assistantResponse.append(token)

                        val lastMessage = chatMessages.lastOrNull()
                        chatMessages = if (lastMessage?.role == HybridMessageRole.Assistant) {
                            chatMessages.dropLast(1) +
                                    lastMessage.copy(content = assistantResponse.toString(), result = null)
                        } else {
                            chatMessages + HybridMessage(
                                content = assistantResponse.toString(),
                                role = HybridMessageRole.Assistant,
                                result = null
                            )
                        }

                        // Scroll to bottom
                        scope.launch {
                            if (isActive) {
                                listState.animateScrollToItem(chatMessages.size - 1)
                            }
                        }
                    }
                )

                if (isActive) {
                    // Update the last assistant message with the result
                    val lastMessage = chatMessages.lastOrNull()
                    if (lastMessage?.role == HybridMessageRole.Assistant) {
                        chatMessages = chatMessages.dropLast(1) +
                                lastMessage.copy(result = result)
                    }
                    isGenerating = false
                }
            } catch (e: Exception) {
                if (isActive) {
                    // Remove typing indicator
                    chatMessages = chatMessages.filter { it.role != HybridMessageRole.Typing }
                    println("Error generating response: ${e.message}")
                    isGenerating = false
                }
            }
        }
    }

    fun transcribeAndSend() {
        if (!isSttModelLoaded) {
            sttStatusText = "Please initialize voice model first"
            return
        }

        scope.launch {
            try {
                isTranscribing = true
                sttStatusText = "Listening... Speak now!"

                val params = SpeechRecognitionParams(
                    sampleRate = 16000,
                    maxDuration = 30000,
                    maxSilenceDuration = 3000,
                )

                val result = withContext(Dispatchers.Default) {
                    stt!!.transcribe(params = params)
                }

                isTranscribing = false

                if (result != null && result.success && !result.text.isNullOrBlank()) {
                    sttStatusText = "Voice ready - tap mic to speak"
                    // Send the transcribed text as a message
                    sendMessage(result.text!!)
                } else {
                    sttStatusText = "Could not understand audio. Try again."
                }
            } catch (e: Exception) {
                isTranscribing = false
                sttStatusText = "Error: ${e.message}"
            }
        }
    }

    fun stopTranscription() {
        scope.launch {
            try {
                sttStatusText = "Processing..."
                withContext(Dispatchers.Default) {
                    stt?.stop()
                }
            } catch (e: Exception) {
                sttStatusText = "Error: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Samantha AI")
                        Text(
                            text = currentDateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            chatMessages = emptyList()
                            hasUserSentMessage = false  // Reset demo state
                        }
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear conversation")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Chat messages area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (chatMessages.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Start a conversation",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (inputMode == InputMode.TEXT) "Type a message below"
                                else "Tap the microphone to speak",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(chatMessages) { message ->
                                when (message.role) {
                                    HybridMessageRole.User -> HybridUserMessageBubble(message)
                                    HybridMessageRole.Assistant -> {
                                        val content = message.content
                                        val isStillThinking = content.contains("<think>") && !content.contains("</think>")
                                        val cleanedContent = cleanHybridContent(content)

                                        if (isStillThinking || cleanedContent.isEmpty()) {
                                            // Show typing indicator while thinking
                                            HybridTypingIndicator()
                                        } else {
                                            HybridAssistantMessageBubble(message)
                                        }
                                    }
                                    HybridMessageRole.Typing -> HybridTypingIndicator()
                                }
                            }
                        }
                    }
                }

                // Input mode toggle and input area
                Surface(
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mode toggle buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = inputMode == InputMode.TEXT,
                                onClick = { inputMode = InputMode.TEXT },
                                label = { Text("Text") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Keyboard,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            FilterChip(
                                selected = inputMode == InputMode.VOICE,
                                onClick = { inputMode = InputMode.VOICE },
                                label = { Text("Voice") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        // Input area based on mode
                        when (inputMode) {
                            InputMode.TEXT -> {
                                // Text input
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Ask anything...") },
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (!isGenerating && !isLmLoading && messageText.isNotBlank()) {
                                                    sendMessage(messageText)
                                                }
                                            }
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Send
                                        )
                                    )

                                    IconButton(
                                        onClick = { sendMessage(messageText) },
                                        enabled = !isGenerating && !isLmLoading && messageText.isNotBlank()
                                    ) {
                                        if (isGenerating || isLmLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send",
                                                modifier = Modifier.size(40.dp),
                                                tint = if (messageText.isNotBlank())
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            )
                                        }
                                    }
                                }
                            }

                            InputMode.VOICE -> {
                                // Voice input
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Loading indicator while model is being downloaded/initialized
                                    if (isSttDownloading || isSttInitializing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(48.dp),
                                            strokeWidth = 4.dp
                                        )
                                    }

                                    // Status text
                                    Text(
                                        sttStatusText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )

                                    // Microphone button (only show if model is loaded)
                                    if (isSttModelLoaded) {
                                        FilledIconButton(
                                            onClick = {
                                                if (isTranscribing) {
                                                    stopTranscription()
                                                } else {
                                                    transcribeAndSend()
                                                }
                                            },
                                            enabled = !isGenerating && !isLmLoading,
                                            modifier = Modifier.size(64.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = if (isTranscribing)
                                                    MaterialTheme.colorScheme.error
                                                else
                                                    MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            if (isTranscribing) {
                                                Icon(
                                                    Icons.Default.Stop,
                                                    contentDescription = "Stop recording",
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Mic,
                                                    contentDescription = "Start recording",
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }  // Close Column
        }  // Close Box with background
    }
}

@Composable
private fun HybridUserMessageBubble(message: HybridMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HybridAssistantMessageBubble(message: HybridMessage) {
    val content = message.content

    // Check if we're still in the thinking phase (has opening tag but no closing tag yet)
    val isStillThinking = content.contains("<think>") && !content.contains("</think>")

    // Get cleaned content (removes thinking tags and their content)
    val cleanedContent = remember(content) { cleanHybridContent(content) }

    // Don't show anything if still thinking or if cleaned content is empty
    if (isStillThinking || cleanedContent.isEmpty()) {
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = cleanedContent,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HybridTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 200)
                        ),
                        label = "dot_scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp * scale)
                            .background(
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}

// Data classes for HybridChat
data class HybridMessage(
    val content: String,
    val role: HybridMessageRole,
    val result: CactusCompletionResult? = null
)

enum class HybridMessageRole {
    User, Assistant, Typing;

    fun toApiRole(): String = when (this) {
        User -> "user"
        Assistant -> "assistant"
        Typing -> "typing"
    }
}

// Helper function
private fun cleanHybridContent(content: String): String {
    // Remove thinking tags and their content
    val withoutThinking = content.replace(Regex("(?s)<think>.*?</think>"), "")
    return withoutThinking
        .replace(Regex("<\\|im_end\\|>"), "")
        .replace(Regex("</s>"), "")
        .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")  // Remove bold **text**
        .replace(Regex("\\*([^*]+)\\*"), "$1")        // Remove italic *text*
        .trim()
}

// Date/time helper function
private fun getCurrentDateTime(context: android.content.Context): String {
    val date = Date()
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date

    val dayOfWeek = DateFormat.format("EEEE", date)
    val month = DateFormat.format("MMMM", date)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    val time = DateFormat.format("HH:mm:ss", date)

    val daySuffix = when (day) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }

    return "$dayOfWeek, $month $day$daySuffix, $year, $time."
}