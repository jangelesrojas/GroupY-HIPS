package com.example.hips

// This screen lets the user choose a JPEG, type a message, and start the embed process.


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay


@Composable
fun EmbedPage(
    theme: AppTheme,
    selectedImageUri: Uri? = null,
    message: String,
    statusText: String? = null,
    capacityBytes: Int? = null,
    successDialogMessage: String? = null,
    onDismissSuccessDialog: () -> Unit = {},
    onMessageChange: (String) -> Unit,
    onBack: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onEmbedClick: () -> Unit
) {
    // Theme-aware colors used by the embed screen.
    val backgroundColor = if (theme == AppTheme.DARK) Color(0xFF0D0D1A) else Color(0xFFF8FAFC)
    val cardPrimary = if (theme == AppTheme.DARK) Color(0xFF1E0A4A) else Color(0xFFF3E8FF)
    val cardSecondary = if (theme == AppTheme.DARK) Color(0xFF13132A) else Color.White
    val iconPrimaryBg = if (theme == AppTheme.DARK) Color(0xFF2D1B6B) else Color(0xFFE9D5FF)
    val iconSecondaryBg = if (theme == AppTheme.DARK) Color(0xFF1E1E3A) else Color(0xFFE5E7EB)
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val subtitleColor = if (theme == AppTheme.DARK) Color(0xFF888888) else Color(0xFF6B7280)
    val bodyColor = if (theme == AppTheme.DARK) Color(0xFFCCCCCC) else Color(0xFF4B5563)
    val borderColor = if (theme == AppTheme.DARK) Color(0xFF2A2A40) else Color(0xFFE5E7EB)
    val accent = Color(0xFF7B4FE0)
    // Capacity comes from the selected JPEG, with the app max as a fallback.
    val actualCapacity = capacityBytes ?: Steganography.MAX_MESSAGE_BYTES
    val messageBytes = message.toByteArray(Charsets.UTF_8).size
    val overLimit = messageBytes > actualCapacity

    // Auto-dismisses the success dialog so the screen does not stay blocked.
    LaunchedEffect(successDialogMessage) {
        // Shows a centered success dialog after the image is embedded and saved.
        if (!successDialogMessage.isNullOrBlank()) {
            delay(1600)
            onDismissSuccessDialog()
        }
    }

    if (!successDialogMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onDismissSuccessDialog,
            confirmButton = {
                TextButton(onClick = onDismissSuccessDialog) {
                    Text("OK")
                }
            },
            title = {
                Text("Embed complete")
            },
            text = {
                Text(successDialogMessage)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = titleColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back", color = titleColor, fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconPrimaryBg, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Embed Message",
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Hide a secret in your JPEG image",
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose how to get your cover image. You can take a photo directly or pick one from your gallery.",
            color = bodyColor,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardPrimary, shape = RoundedCornerShape(12.dp))
                .clickable { onTakePhotoClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(iconPrimaryBg, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Take a Photo",
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Capture a new JPEG image with the camera",
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardSecondary, shape = RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable { onPickFromGalleryClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(iconSecondaryBg, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Choose from Gallery",
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Select an existing JPEG image from your device",
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardSecondary, shape = RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0x22F59E0B), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "JPEG output enabled",
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This flow is set up for JPEG carrier images and will save the embedded result as a JPEG.",
                    color = bodyColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        if (selectedImageUri != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selected image",
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardSecondary),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPickFromGalleryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Image")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Secret message",
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val messageBytes = message.toByteArray(Charsets.UTF_8).size
            val overLimit = messageBytes > Steganography.MAX_MESSAGE_BYTES

            OutlinedTextField(
                value = message,
                onValueChange = { newValue ->
                    onMessageChange(newValue)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter secret message") },
                supportingText = {
                    Text("$messageBytes / $actualCapacity bytes")
                },
                isError = overLimit,
                minLines = 4,
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = titleColor,
                    unfocusedTextColor = titleColor,
                    cursorColor = accent,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = borderColor,
                    focusedLabelColor = accent,
                    unfocusedLabelColor = subtitleColor,
                    focusedSupportingTextColor = subtitleColor,
                    unfocusedSupportingTextColor = subtitleColor
                )
            )

            if (overLimit) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Message is too large for this image. Keep it at $actualCapacity bytes or less.",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp
                )
            }

            ///Text(  /// debug checkpoint unable to embed due to capacity check failure
            ///text = "Debug capacity: ${capacityBytes ?: -1}",
            ///fontSize = 12.sp,
            ///color = subtitleColor
            ///)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEmbedClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = message.isNotBlank() && !overLimit && capacityBytes != null
            ) {
                Text("Embed into JPEG")
            }

            if (!statusText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = statusText,
                        color = bodyColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardSecondary, RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "No image selected yet. Pick a JPEG from the gallery or take a photo to continue.",
                    color = bodyColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            if (!statusText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = statusText,
                        color = bodyColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}