package com.example.hips

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun EmbedPage(
    theme: AppTheme,
    selectedImageUri: Uri? = null,
    onBack: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    var secretMessage by remember { mutableStateOf("") }
    var isEncoding by remember { mutableStateOf(false) }
    var encodingSuccess by remember { mutableStateOf(false) }

    val backgroundColor = if (theme == AppTheme.DARK) Color(0xFF0D0D1A) else Color(0xFFF8FAFC)
    val cardPrimary = if (theme == AppTheme.DARK) Color(0xFF1E0A4A) else Color(0xFFF3E8FF)
    val cardSecondary = if (theme == AppTheme.DARK) Color(0xFF13132A) else Color.White
    val iconPrimaryBg = if (theme == AppTheme.DARK) Color(0xFF2D1B6B) else Color(0xFFE9D5FF)
    val iconSecondaryBg = if (theme == AppTheme.DARK) Color(0xFF1E1E3A) else Color(0xFFE5E7EB)
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val subtitleColor = if (theme == AppTheme.DARK) Color(0xFF888888) else Color(0xFF6B7280)
    val bodyColor = if (theme == AppTheme.DARK) Color(0xFFCCCCCC) else Color(0xFF4B5563)
    val accent = Color(0xFF7B4FE0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 40.dp)
            .verticalScroll(rememberScrollState())
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
                Text("Embed Message", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Hide a secret in your image", color = subtitleColor, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedImageUri != null && !encodingSuccess) {
            Text("Selected Image:", color = titleColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, accent, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Secret Message:", color = titleColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = secretMessage,
                onValueChange = { secretMessage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Type your secret message here...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = if (theme == AppTheme.DARK) Color(0xFF333333) else Color(0xFFD1D5DB),
                    focusedTextColor = titleColor,
                    unfocusedTextColor = titleColor,
                    cursorColor = accent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (secretMessage.isBlank()) {
                        Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isEncoding = true
                    val bitmap = BitmapUtils.loadBitmapFromUri(context, selectedImageUri)
                    if (bitmap == null) {
                        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                        isEncoding = false
                        return@Button
                    }
                    val pixels = BitmapUtils.bitmapToPixels(bitmap)
                    val encodedPixels = Steganography.encodeMessage(pixels, secretMessage)
                    if (encodedPixels == null) {
                        Toast.makeText(context, "Image too small for this message", Toast.LENGTH_SHORT).show()
                        isEncoding = false
                        return@Button
                    }
                    val encodedBitmap = BitmapUtils.pixelsToBitmap(encodedPixels, bitmap.width, bitmap.height)
                    val savedUri = BitmapUtils.saveBitmapToGallery(context, encodedBitmap, "HIPS_encoded_${System.currentTimeMillis()}")
                    isEncoding = false
                    if (savedUri != null) {
                        encodingSuccess = true
                        Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = secretMessage.isNotBlank() && !isEncoding,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(
                    text = if (isEncoding) "Encoding..." else "Embed & Save to Gallery",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else if (encodingSuccess) {
            Spacer(modifier = Modifier.height(40.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Message Embedded!", color = Color(0xFF00FF7F), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Your encoded image has been saved to your gallery in the HIPS folder as a PNG.",
                    color = bodyColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        encodingSuccess = false
                        secretMessage = ""
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text("Back to Home", color = Color.White)
                }
            }
        } else {
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
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Take a Photo", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Use your camera to capture a new image", color = subtitleColor, fontSize = 13.sp, lineHeight = 18.sp)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardSecondary, shape = RoundedCornerShape(12.dp))
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
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Choose from Gallery", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Select an existing image", color = subtitleColor, fontSize = 13.sp)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A1A00), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFAA00), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "The output image will be saved as a lossless PNG to preserve the hidden data.",
                    color = Color(0xFFFFAA00),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
