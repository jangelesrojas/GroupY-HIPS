package com.example.hips

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmbedPage(
    theme: AppTheme,
    onBack: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit

) {
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
    ) {
        // Back button
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

        // Title row
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

        Spacer(modifier = Modifier.height(24.dp))


        Spacer(modifier = Modifier.height(20.dp))

        // Description test below title
        Text(
            text = "Choose how to get your cover image. You can take a photo directly or pick one from your gallery.",
            color = bodyColor,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Button for Take a Photo
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
                Text(
                    "Use your camera to capture a new image",
                    color = subtitleColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button for Choose from Gallery
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardSecondary, shape = RoundedCornerShape(12.dp))
                .clickable { onPickFromGalleryClick()}
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
                Text(
                    "Select an existing image or JPEG",
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Orange text box at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A1A00), shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFFAA00),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Supports JPEG images only. The output image will also be a JPEG.",
                color = Color(0xFFFFAA00),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            @Composable
            fun CameraCaptureScreen(
                theme: AppTheme,
                onBack: () -> Unit,
                onPhotoCaptured: (Uri) -> Unit
            ){

            }
        }
    }
}
