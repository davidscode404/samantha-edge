package com.cactus.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cactus.services.CactusTelemetry
import com.cactus.example.theme.AppTheme
import com.cactus.example.pages.ABasicPage
import com.cactus.example.pages.BasicCompletionPage
import com.cactus.example.pages.ChatPage
import com.cactus.example.pages.EmbeddingPage
import com.cactus.example.pages.FetchModelsPage
import com.cactus.example.pages.FunctionCallingPage
import com.cactus.example.pages.HybridChatPage
import com.cactus.example.pages.HybridCompletionPage
import com.cactus.example.pages.MapPage
import com.cactus.example.pages.NewVisionPage
import com.cactus.example.pages.StreamingCompletionPage
import com.cactus.example.pages.TimePage
import com.cactus.example.pages.TranscriptionPage
import com.cactus.example.pages.TitlePage
import com.cactus.example.pages.VisionPage
import kotlinx.coroutines.delay

// Sealed class for navigation destinations
sealed class AppScreen {
    object Splash : AppScreen()
    object Menu : AppScreen()
    object HybridChat : AppScreen()
    object Map : AppScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    // Start on Splash screen
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

    // Set telemetry token
    LaunchedEffect(Unit) {
        CactusTelemetry.setTelemetryToken("a83c7f7a-43ad-4823-b012-cbeb587ae788")
    }

    AppTheme {
        when (currentScreen) {
            AppScreen.Splash -> {
                TitlePage(onBack = { })

                // Auto-navigate to HybridChat after 5 seconds
                LaunchedEffect(Unit) {
                    delay(5000L)
                    currentScreen = AppScreen.HybridChat
                }
            }

            AppScreen.HybridChat -> {
                HybridChatPage(
                    onBack = { currentScreen = AppScreen.Menu },
                    onNavigateToMap = { currentScreen = AppScreen.Map }
                )
            }

            AppScreen.Map -> {
                MapPage(
                    onBack = { currentScreen = AppScreen.HybridChat }
                )
            }

            AppScreen.Menu -> {
                MenuScreen(
                    onNavigate = { screen -> currentScreen = screen }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScreen(onNavigate: (AppScreen) -> Unit) {
    data class ExampleItem(
        val title: String,
        val description: String,
        val screen: AppScreen
    )

    val examples = listOf(
        ExampleItem(
            title = "Samantha AI",
            description = "Chat with voice and text input",
            screen = AppScreen.HybridChat
        ),
        ExampleItem(
            title = "Map",
            description = "View map with route",
            screen = AppScreen.Map
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cactus Examples") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(examples) { example ->
                Card(
                    onClick = { onNavigate(example.screen) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = example.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = example.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}