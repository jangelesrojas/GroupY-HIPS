package com.example.hips

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val showUnlockScreen = true

            if (showUnlockScreen) {
                ChangePinGestureScreen(
                    onBack = { /* handle back */ },
                    // theme = AppTheme.DARK,
                    // onToggleTheme = { /* toggle theme logic */ },
                    // onChangePinGesture = { /* navigate to unlock screen */ }
                )
            } else {
                CoverAppScreen(
                    onUnlock = {}
                )
            }
        }
    }
}