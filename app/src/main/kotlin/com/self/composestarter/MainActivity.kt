package com.example.composestarter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import android.view.WindowManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- THE "CHROME" SETUP ---
        
        // 1. Tell the Window NOT to decorate the system bars automatically.
        // This ensures your color settings below are respected and not overwritten by themes.
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 2. Set the physical colors to Pure White
        // We use android.graphics.Color.WHITE (Standard Android Color) for the Window APIs
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE
        window.decorView.setBackgroundColor(android.graphics.Color.WHITE)

        // 3. Force the Status Bar & Nav Bar Icons to be BLACK
        // "isAppearanceLightStatusBars = true" means "The bar is Light (White), so make icons Dark (Black)"
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        setContent {
            // This is your entry point
            AppEntryPoint()
        }
    }
}

@Composable
fun AppEntryPoint() {
    // A simple container filling the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Compose White
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hello, Jetpack Compose!",
            color = Color.Black,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppEntryPoint()
}