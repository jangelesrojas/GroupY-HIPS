package com.example.hips

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// This file is used to ask for camera permission for HIPS when the user tries to use the camera functionality.
// Not currently hooked up, add this function when camera is implemented.

@Composable
fun CameraPermissionHandler(onPermissionGranted: () -> Unit) {

    // Starts system prompt when called. Asks user to Allow/Deny camera access.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onPermissionGranted()
    }

    // Triggers launch effect.
    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }
}