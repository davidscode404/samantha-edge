package com.cactus.example.pages

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePage(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentDateTime by remember { mutableStateOf(getCurrentDateTime(context)) }

    // Auto-refresh every second
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = getCurrentDateTime(context)
            delay(1000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Time Page") },
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

                    Text(currentDateTime)
                }
            }
        }
    }
}

private fun getCurrentDateTime(context: android.content.Context): String {
    val date = Date()
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date

    val dayOfWeek = android.text.format.DateFormat.format("EEEE", date)
    val month = android.text.format.DateFormat.format("MMMM", date)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    val time = android.text.format.DateFormat.format("HH:mm:ss", date)

    val daySuffix = when (day) {
        1, 21, 31 -> "st"
        2, 22 -> "nd"
        3, 23 -> "rd"
        else -> "th"
    }

    return "$dayOfWeek, $month $day$daySuffix, $year, $time."
}