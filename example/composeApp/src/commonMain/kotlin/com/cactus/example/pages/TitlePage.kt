package com.cactus.example.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cactus.example.R

@Composable
fun TitlePage(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Samantha EDGE",
                fontSize = 40.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OFFLINE AI ASSISTANT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.dp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}