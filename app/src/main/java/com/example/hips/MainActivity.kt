package com.example.hips

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CoverAppScreen(
                onUnlock = {
                    // TODO: navigate to next screen after unlock
                }
            )
        }
    }

    companion object {
        init {
            System.loadLibrary("hips")
        }
    }
}