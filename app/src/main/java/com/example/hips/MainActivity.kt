package com.example.hips

// MainActivity controls the screen navigation and connects the UI screens to the steganography logic.


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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Main activity for the app. It owns the Compose content and screen routing.
class MainActivity : ComponentActivity() {

    // Creates a phone-style timestamp so saved images have a normal looking file name.
    private fun makePhoneStyleTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    }

    // Saves the output JPEG into the main Pictures folder so it appears in the gallery.
    private fun saveJpegToGallery(context: Context, sourceFile: File): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${makePhoneStyleTimestamp()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
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



    // Android entry point where the Compose UI and app state are created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // SharedPreferences stores the local unlock settings.
            val prefs = getSharedPreferences("hips_auth", Context.MODE_PRIVATE)
            val context = LocalContext.current

            val lifecycleOwner = LocalLifecycleOwner.current
            val demoScope = rememberCoroutineScope()

            // currentScreen controls which page is shown in the manual navigation flow.
            var currentScreen by rememberSaveable { mutableStateOf("cover") }
            var appTheme by rememberSaveable { mutableStateOf(AppTheme.DARK) }
            var capturedImageUri by rememberSaveable { mutableStateOf<String?>(null) }

            // State used by the embed flow.
            var embedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
            var embedMessage by rememberSaveable { mutableStateOf("") }
            var embedStatus by rememberSaveable { mutableStateOf<String?>(null) }

            // State used by the extract flow.
            var extractImageUri by rememberSaveable { mutableStateOf<String?>(null) }
            var extractStatus by rememberSaveable { mutableStateOf<String?>(null) }
            var extractedMessage by rememberSaveable { mutableStateOf<String?>(null) }

            // State used for embed feedback and selected image capacity.
            var embedSuccessDialog by rememberSaveable { mutableStateOf<String?>(null) }
            var embedCapacityBytes by rememberSaveable { mutableStateOf<Int?>(null) }

            // Gallery picker for the embed flow. It also checks JPEG capacity after selection.
            val pickEmbedImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    embedImageUri = uri.toString()
                    embedStatus = "Checking JPEG capacity..."
                    embedSuccessDialog = null
                    embedCapacityBytes = null

                    demoScope.launch {
                        try {
                            Log.d("HIPS_DEMO", "Starting capacity check for gallery image")

                            val capacity = withContext(Dispatchers.IO) {
                                val inputFile = StegoFileHelper.copyUriToCacheJpeg(
                                    context = context,
                                    uri = uri,
                                    fileName = "capacity_check.jpg"
                                )

                                Steganography.getEmbedCapacityBytes(inputFile.absolutePath)
                            }

                            embedCapacityBytes = capacity
                            embedStatus = null

                            Log.d("HIPS_DEMO", "Capacity check finished: $capacity bytes")
                        } catch (e: Exception) {
                            Log.e("HIPS_DEMO", "Capacity check failed", e)

                            embedCapacityBytes = null
                            embedStatus = "Could not read JPEG capacity: ${e.message}"
                        }
                    }
                }
            }

            // Gallery picker for the extract flow.
            val pickExtractImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    extractImageUri = uri.toString()
                    extractStatus = null
                    extractedMessage = null
                }
            }

            // Clears embed data when leaving the embed screen or resetting the session.
            fun clearEmbedState() {
                capturedImageUri = null
                embedImageUri = null
                embedMessage = ""
                embedStatus = null
            }

            // Clears extract data when leaving the extract screen or resetting the session.
            fun clearExtractState() {
                extractImageUri = null
                extractStatus = null
                extractedMessage = null
            }

            // Sends the app back to the cover screen and removes sensitive temporary state.
            fun resetSecureSession() {
                clearEmbedState()
                clearExtractState()
                currentScreen = "cover"
            }

            // Restores default app settings, including the default PIN and dark theme.
            fun resetAppSettings() {
                prefs.edit()
                    .putString("hips-auth-method", "pin")
                    .putString("hips-pin", "1234")
                    .putString("hips-pattern", "0,1,2,4,8")
                    .apply()

                appTheme = AppTheme.DARK
            }

            // If the app is backgrounded, clear sensitive state and return to the cover app.
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        clearEmbedState()
                        clearExtractState()
                        currentScreen = "cover"
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // Basic manual navigation between screens.
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
                            resetSecureSession()
                        }
                    )
                }

                "settings" -> {
                    SettingsPage(
                        onBack = { currentScreen = "realMain" },
                        theme = appTheme,
                        onToggleTheme = {
                            appTheme = if (appTheme == AppTheme.DARK) {
                                AppTheme.LIGHT
                            } else {
                                AppTheme.DARK
                            }
                        },
                        onResetApp = { resetAppSettings() },
                        onChangePinGesture = { currentScreen = "changePinGesture" },
                        onOpenAppInfo = { currentScreen = "appInfo" }
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

                "appInfo" -> {
                    AppInfoPage(
                        theme = appTheme,
                        onBack = { currentScreen = "settings" }
                    )
                }

                "embed" -> {
                    EmbedPage(
                        theme = appTheme,
                        selectedImageUri = embedImageUri?.let { Uri.parse(it) },
                        message = embedMessage,
                        statusText = embedStatus,
                        capacityBytes = embedCapacityBytes,
                        successDialogMessage = embedSuccessDialog,
                        onDismissSuccessDialog = { embedSuccessDialog = null },
                        onMessageChange = {
                            embedMessage = it
                            embedStatus = null
                        },
                        onBack = {
                            clearEmbedState()
                            currentScreen = "realMain"
                        },
                        onTakePhotoClick = { currentScreen = "cameraPermission" },
                        onPickFromGalleryClick = {
                            pickEmbedImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        // Validates the message and selected JPEG before calling the native embed function.
                        onEmbedClick = click@{
                            val imageUriString = embedImageUri

                            if (imageUriString.isNullOrBlank()) {
                                embedStatus = "Please select a JPEG image first."
                                return@click
                            }

                            if (embedMessage.isBlank()) {
                                embedStatus = "Please enter a message first."
                                return@click
                            }

                            val messageBytes = Steganography.getUtf8Size(embedMessage)
                            val availableCapacity = embedCapacityBytes ?: 0

                            if (messageBytes > availableCapacity) {
                                embedStatus = "Message is too large for this JPEG. Capacity: $availableCapacity bytes."
                                return@click
                            }

                            embedStatus = "Embedding message..."
                            embedSuccessDialog = null

                            demoScope.launch {
                                try {
                                    Log.d("HIPS_DEMO", "Starting embed")

                                    val savedUri = withContext(Dispatchers.IO) {
                                        val inputUri = Uri.parse(imageUriString)

                                        val inputFile = StegoFileHelper.copyUriToCacheJpeg(
                                            context = context,
                                            uri = inputUri,
                                            fileName = "embed_input.jpg"
                                        )

                                        val outputFile = File(context.cacheDir, "stego_output.jpg")

                                        val success = Steganography.embedJpegMessage(
                                            inputPath = inputFile.absolutePath,
                                            outputPath = outputFile.absolutePath,
                                            message = embedMessage
                                        )

                                        if (success) {
                                            saveJpegToGallery(context, outputFile)
                                        } else {
                                            null
                                        }
                                    }

                                    if (savedUri != null) {
                                        Log.d("HIPS_DEMO", "Embed finished and saved: $savedUri")

                                        clearEmbedState()
                                        embedSuccessDialog = "Message embedded successfully and saved to gallery."
                                        embedStatus = null
                                    } else {
                                        Log.d("HIPS_DEMO", "Embed failed or save returned null")

                                        embedStatus = "Embedding failed. Try a larger JPEG or a shorter message."
                                    }
                                } catch (e: Exception) {
                                    Log.e("HIPS_DEMO", "Embedding failed", e)

                                    embedStatus = "Embedding failed: ${e.message}"
                                }
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
                            clearExtractState()
                            currentScreen = "realMain"
                        },
                        onSelectImageClick = {
                            pickExtractImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        // Copies the selected JPEG to cache and calls the native extract function.
                        onContinueClick = click@{
                            val imageUriString = extractImageUri

                            if (imageUriString.isNullOrBlank()) {
                                extractStatus = "Please select a JPEG image first."
                                extractedMessage = null
                                return@click
                            }

                            extractStatus = "Extracting message..."
                            extractedMessage = null

                            demoScope.launch {
                                try {
                                    Log.d("HIPS_DEMO", "Starting extract")

                                    val result = withContext(Dispatchers.IO) {
                                        val inputUri = Uri.parse(imageUriString)

                                        val inputFile = StegoFileHelper.copyUriToCacheJpeg(
                                            context = context,
                                            uri = inputUri,
                                            fileName = "extract_input.jpg"
                                        )

                                        Steganography.extractJpegMessage(inputFile.absolutePath)
                                    }

                                    if (result != null) {
                                        Log.d("HIPS_DEMO", "Extract finished successfully")

                                        extractedMessage = result
                                        extractStatus = "Message extracted successfully."
                                    } else {
                                        Log.d("HIPS_DEMO", "Extract finished, but no message found")

                                        extractedMessage = null
                                        extractStatus = "No HIPS message was found in this JPEG."
                                    }
                                } catch (e: Exception) {
                                    Log.e("HIPS_DEMO", "Extraction failed", e)

                                    extractedMessage = null
                                    extractStatus = "Extraction failed: ${e.message}"
                                }
                            }
                        }
                    )
                }

                "camera" -> {
                    CameraScreen(
                        onBack = {
                            currentScreen = "embed"
                        },
                        // After taking a photo, check its capacity and then return to the embed screen.
                        onPhotoCaptured = { uri ->
                            embedImageUri = uri.toString()
                            embedStatus = "Checking JPEG capacity..."
                            embedCapacityBytes = null

                            demoScope.launch {
                                try {
                                    Log.d("HIPS_DEMO", "Starting capacity check for camera image")

                                    val capacity = withContext(Dispatchers.IO) {
                                        val inputFile = StegoFileHelper.copyUriToCacheJpeg(
                                            context = context,
                                            uri = uri,
                                            fileName = "camera_capacity_check.jpg"
                                        )

                                        Steganography.getEmbedCapacityBytes(inputFile.absolutePath)
                                    }

                                    embedCapacityBytes = capacity
                                    embedStatus = null

                                    Log.d("HIPS_DEMO", "Camera capacity check finished: $capacity bytes")
                                } catch (e: Exception) {
                                    Log.e("HIPS_DEMO", "Camera capacity check failed", e)

                                    embedCapacityBytes = null
                                    embedStatus = "Could not read JPEG capacity: ${e.message}"
                                }

                                currentScreen = "embed"
                            }
                        }
                    )
                }
            }
        }
    }

}