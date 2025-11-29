package com.cactus.example.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cactus.*
import com.cactus.example.rememberFilePickerLauncher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewVisionPage(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val lm = remember { CactusLM() }

    val selectedModel = "local-lfm2-vl-450m"
    var isModelLoaded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var outputText by remember { mutableStateOf("Pick an image to get started.") }
    var streamingResponse by remember { mutableStateOf("") }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberFilePickerLauncher(
        onFileSelected = { path ->
            if (path != null) {
                selectedImagePath = path
                if (!isModelLoaded && !isLoading) {
                    // Start loading model when image is picked
                    scope.launch {
                        isLoading = true
                        outputText = "Loading vision model..."
                        try {
                            lm.downloadModel(model = selectedModel)
                            outputText = "Initializing model..."
                            lm.initializeModel(CactusInitParams(model = selectedModel))
                            isModelLoaded = true
                            isLoading = false
                            outputText = "Ready! Click \"Analyze Image\" to process."
                        } catch (e: Exception) {
                            isLoading = false
                            outputText = "Error loading model: ${e.message}"
                        }
                    }
                } else if (isModelLoaded) {
                    outputText = "Image selected! Click \"Analyze Image\" to process."
                }
            } else {
                outputText = "No image selected or an error occurred."
            }
        },
        mimeType = "image/*"
    )

    fun analyzeImage() {
        if (!isModelLoaded) {
            outputText = "Please wait for model to load."
            return
        }

        if (selectedImagePath == null) {
            outputText = "Please pick an image first."
            return
        }

        scope.launch {
            isGenerating = true
            streamingResponse = ""
            outputText = "Analyzing image..."
            var firstToken = true

            try {
                val resp = lm.generateCompletion(
                    messages = listOf(
                        ChatMessage("You are a helpful AI assistant that can analyze images.", "system"),
                        ChatMessage(
                            content = "Describe this image in detail.",
                            role = "user",
                            images = listOf(selectedImagePath!!)
                        )
                    ),
                    params = CactusCompletionParams(
                        maxTokens = 300
                    ),
                    onToken = { token, _ ->
                        if (firstToken) {
                            streamingResponse = ""
                            firstToken = false
                        }
                        streamingResponse += token
                    }
                )

                if (isActive) {
                    if (resp != null && resp.success) {
                        outputText = "Image analysis completed!"
                    } else {
                        outputText = "Failed to analyze image."
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    outputText = "Error analyzing image: ${e.message}"
                }
            } finally {
                if (isActive) {
                    isGenerating = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            lm.unload()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vision Example") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading indicator
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Loading vision model...")
                }
            }

            // Pick Image and Analyze buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { filePickerLauncher.launch() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (selectedImagePath != null) "Change Image" else "Pick Image")
                }

                Button(
                    onClick = { analyzeImage() },
                    enabled = !isLoading && !isGenerating && isModelLoaded && selectedImagePath != null,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isGenerating) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Analyzing...")
                        }
                    } else {
                        Text("Analyze Image")
                    }
                }
            }

            // Output section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Output:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(outputText)

                    if (streamingResponse.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "Response:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = streamingResponse,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}