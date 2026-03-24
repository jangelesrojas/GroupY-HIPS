package com.example.hips

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = getSharedPreferences("hips_auth", Context.MODE_PRIVATE)

            var currentScreen by rememberSaveable { mutableStateOf("cover") }
            var appTheme by rememberSaveable { mutableStateOf(AppTheme.DARK) }

            fun resetAppSettings() {
                prefs.edit()
                    .putString("hips-auth-method", "pin")
                    .putString("hips-pin", "1234")
                    .putString("hips-pattern", "0,1,2,4,8")
                    .apply()

                appTheme = AppTheme.DARK
            }

            when (currentScreen) {
                "cover" -> {
                    CoverAppScreen(
                        onUnlock = {
                            currentScreen = "unlock"
                        }
                    )
                }

                "unlock" -> {
                    UnlockScreen(
                        theme = appTheme,
                        onSuccess = {
                            currentScreen = "realMain"
                        },
                        onBack = {
                            currentScreen = "cover"
                        }
                    )
                }

                "realMain" -> {
                    RealMain(
                        theme = appTheme,
                        onOpenSettings = {
                            currentScreen = "settings"
                        },
                        onOpenEmbed = {
                            currentScreen = "embed"
                        },
                        onOpenExtract = {
                            currentScreen = "extract"
                        },
                        onExit = {
                            currentScreen = "cover"
                        }
                    )
                }

                "settings" -> {
                    SettingsPage(
                        onBack = {
                            currentScreen = "realMain"
                        },
                        theme = appTheme,
                        onToggleTheme = {
                            appTheme = if (appTheme == AppTheme.DARK) {
                                AppTheme.LIGHT
                            } else {
                                AppTheme.DARK
                            }
                        },
                        onResetApp = {
                            resetAppSettings()
                        },
                        onChangePinGesture = {
                            currentScreen = "changePinGesture"
                        }
                    )
                }

                "changePinGesture" -> {
                    ChangePinGestureScreen(
                        theme = appTheme,
                        onBack = {
                            currentScreen = "settings"
                        }
                    )
                }

                "embed" -> {
                    EmbedPage(
                        theme = appTheme,
                        onBack = {
                            currentScreen = "realMain"
                        }
                    )
                }

                "extract" -> {
                    ExtractPage(
                        theme = appTheme,
                        onBack = {
                            currentScreen = "realMain"
                        },
                        onSelectImageClick = {
                            // TO DO: open image picker here
                        },
                        onContinueClick = {
                            // TO DO: move to extract confirm/reveal step
                        }
                    )

                }
            }
        }
    }
}