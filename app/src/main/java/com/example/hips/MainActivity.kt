package com.example.hips

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = getSharedPreferences("hips_auth", Context.MODE_PRIVATE)

            var currentScreen by remember { mutableStateOf("cover") }
            var appTheme by remember { mutableStateOf(AppTheme.DARK) }
            var capturedImageUri by remember { mutableStateOf<String?>(null) }

            val pickImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    capturedImageUri = uri.toString()
                }
            }

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

                "cameraPermission" -> {
                    CameraPermissionHandler(
                        onPermissionGranted = {
                            currentScreen = "camera"
                        },
                        onBack = {
                            currentScreen = "embed"
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
                        },
                        onTakePhotoClick = {
                            currentScreen = "cameraPermission"
                        },
                        onPickFromGalleryClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
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
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onContinueClick = {
                        }
                    )
                }

                "camera" -> {
                    CameraScreen(
                        onBack = {
                            currentScreen = "embed"
                        },
                        onPhotoCaptured = { uri ->
                            capturedImageUri = uri.toString()
                            currentScreen = "embed"
                        }
                    )
                }
            }
        }
    }
}