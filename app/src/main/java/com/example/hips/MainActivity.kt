package com.example.hips

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.io.File
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
class MainActivity : ComponentActivity() {

    private fun saveJpegToGallery(context: Context, sourceFile: File): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "hips_stego_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HIPS")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        resolver.openOutputStream(uri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: return null

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = getSharedPreferences("hips_auth", Context.MODE_PRIVATE)
            val context = LocalContext.current
            var currentScreen by rememberSaveable { mutableStateOf("cover") }
            var appTheme by rememberSaveable { mutableStateOf(AppTheme.DARK) }
            var capturedImageUri by rememberSaveable { mutableStateOf<String?>(null) }

            var embedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
            var embedMessage by rememberSaveable { mutableStateOf("") }
            var embedStatus by rememberSaveable { mutableStateOf<String?>(null) }

            var extractImageUri by rememberSaveable { mutableStateOf<String?>(null) }
            var extractStatus by rememberSaveable { mutableStateOf<String?>(null) }
            var extractedMessage by rememberSaveable { mutableStateOf<String?>(null) }

            val pickEmbedImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    embedImageUri = uri.toString()
                    embedStatus = null
                }
            }

            val pickExtractImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    extractImageUri = uri.toString()
                    extractStatus = null
                    extractedMessage = null
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
                        selectedImageUri = embedImageUri?.let { Uri.parse(it) },
                        message = embedMessage,
                        statusText = embedStatus,
                        onMessageChange = {
                            embedMessage = it
                            embedStatus = null
                        },
                        onBack = {
                            currentScreen = "realMain"
                        },
                        onTakePhotoClick = {
                            currentScreen = "cameraPermission"
                        },
                        onPickFromGalleryClick = {
                            pickEmbedImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onEmbedClick = click@{
                            val imageUriString = embedImageUri
                            if (imageUriString.isNullOrBlank()) {
                                embedStatus = "Please select a JPEG image first."
                                return@click
                            }

                            val messageBytes = Steganography.getUtf8Size(embedMessage)
                            if (messageBytes > Steganography.MAX_MESSAGE_BYTES) {
                                embedStatus = "Message is too large. Keep it at 100 bytes or less."
                                return@click
                            }

                            try {
                                val inputUri = Uri.parse(imageUriString)

                                val inputStream = context.contentResolver.openInputStream(inputUri)
                                if (inputStream == null) {
                                    embedStatus = "Could not open selected image."
                                    return@click
                                }

                                val inputFile = File(context.cacheDir, "embed_input.jpg")
                                inputStream.use { input ->
                                    inputFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                val capacityBytes = Steganography.getEmbedCapacityBytes(inputFile.absolutePath)
                                if (messageBytes > capacityBytes) {
                                    embedStatus = "This JPEG is too small. Capacity: $capacityBytes bytes."
                                    return@click
                                }

                                val outputFile = File(context.cacheDir, "stego_output.jpg")

                                val success = Steganography.embedJpegMessage(
                                    inputPath = inputFile.absolutePath,
                                    outputPath = outputFile.absolutePath,
                                    message = embedMessage
                                )

                                if (success) {
                                    val savedUri = saveJpegToGallery(context, outputFile)
                                    embedStatus = if (savedUri != null) {
                                        "Message embedded successfully and saved to gallery."
                                    } else {
                                        "Embedded, but failed to save to gallery."
                                    }
                                } else {
                                    embedStatus = "Embedding failed. Try a larger JPEG or a shorter message."
                                }

                            } catch (e: Exception) {
                                embedStatus = "Embedding failed: ${e.message}"
                            }
                        }
                    )
                }

                "extract" -> {
                    ExtractPage(
                        theme = appTheme,
                        selectedImageUri = extractImageUri?.let { Uri.parse(it) },
                        selectedImageName = "Selected image",
                        statusText = extractStatus,
                        extractedMessage = extractedMessage,
                        onBack = {
                            extractImageUri = null
                            extractStatus = null
                            extractedMessage = null
                            currentScreen = "realMain"
                        },
                        onSelectImageClick = {
                            pickExtractImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onContinueClick = click@{
                            val imageUriString = extractImageUri
                            if (imageUriString.isNullOrBlank()) {
                                extractStatus = "Please select a JPEG image first."
                                extractedMessage = null
                                return@click
                            }

                            try {
                                val inputUri = Uri.parse(imageUriString)
                                val inputFile = StegoFileHelper.copyUriToCacheJpeg(
                                    context = context,
                                    uri = inputUri,
                                    fileName = "extract_input.jpg"
                                )

                                val result = Steganography.extractJpegMessage(inputFile.absolutePath)

                                if (result != null) {
                                    extractedMessage = result
                                    extractStatus = "Message extracted successfully."
                                } else {
                                    extractedMessage = null
                                    extractStatus = "No HIPS message was found in this JPEG."
                                }
                            } catch (e: Exception) {
                                extractedMessage = null
                                extractStatus = "Extraction failed: ${e.message}"
                            }
                        }
                    )
                }

                "camera" -> {
                    CameraScreen(
                        onBack = {
                            currentScreen = "embed"
                        },
                        onPhotoCaptured = { uri ->
                            embedImageUri = uri.toString()
                            embedStatus = null
                            currentScreen = "embed"
                        }
                    )
                }
            }
        }
    }

}