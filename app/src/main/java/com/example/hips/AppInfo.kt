package com.example.hips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppInfoPage(
    theme: AppTheme,
    onBack: () -> Unit
) {
    // These colors switch based on the current app theme.
    // This keeps the App Info page matching the rest of the app.
    val backgroundColor = if (theme == AppTheme.DARK) Color(0xFF0A0A0F) else Color(0xFFF9FAFB)
    val headerBorderColor = if (theme == AppTheme.DARK) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFE5E7EB)
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val subtitleColor = if (theme == AppTheme.DARK) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val bodyColor = if (theme == AppTheme.DARK) Color(0xFFCBD5E1) else Color(0xFF374151)
    val cardColor = if (theme == AppTheme.DARK) Color(0xFF0F172A).copy(alpha = 0.65f) else Color.White
    val borderColor = if (theme == AppTheme.DARK) Color(0xFF1E293B) else Color(0xFFE5E7EB)
    val iconBg = if (theme == AppTheme.DARK) Color(0xFF1E293B) else Color(0xFFF3F4F6)
    val iconTint = if (theme == AppTheme.DARK) Color(0xFFC4B5FD) else Color(0xFF7C3AED)

    // Main page container.
    // The background color fills the full screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header section with the back button and page title.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .border(0.5.dp, headerBorderColor)
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button returns the user to the Settings page.
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (theme == AppTheme.DARK) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFE5E7EB))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (theme == AppTheme.DARK) Color(0xFF94A3B8) else Color(0xFF4B5563),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                // Page title and subtitle.
                Column {
                    Text(
                        text = "App Info",
                        color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sending and extracting messages",
                        color = subtitleColor,
                        fontSize = 12.sp
                    )
                }
            }

            // Scrollable content section.
            // This is useful because the README-style text can grow later.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Explains the basic purpose of the app.
                InfoSection(
                    theme = theme,
                    title = "What HIPS Does",
                    iconType = "info",
                    cardColor = cardColor,
                    borderColor = borderColor,
                    titleColor = titleColor,
                    bodyColor = bodyColor,
                    iconBg = iconBg,
                    iconTint = iconTint,
                    body = "HIPS hides a short secret message inside a JPEG image. The image should still look like the base picture, but the app can scan it later and try to extract the hidden message."
                )

                // Explains the safest way to send an embedded image.
                InfoSection(
                    theme = theme,
                    title = "Best Way to Send Images",
                    iconType = "mail",
                    cardColor = cardColor,
                    borderColor = borderColor,
                    titleColor = titleColor,
                    bodyColor = bodyColor,
                    iconBg = iconBg,
                    iconTint = iconTint,
                    body = "For the best results, send the embedded JPEG as a file attachment. Gmail attachments have worked well because they usually preserve the file instead of heavy recompression like a normal chat image."
                )

                // Lists transfer methods that are more likely to preserve the JPEG.
                InfoSection(
                    theme = theme,
                    title = "Mediums That May Work",
                    iconType = "image",
                    cardColor = cardColor,
                    borderColor = borderColor,
                    titleColor = titleColor,
                    bodyColor = bodyColor,
                    iconBg = iconBg,
                    iconTint = iconTint,
                    body = "Reliable options are usually file-based sharing methods, such as Gmail attachments, Google Drive links, USB transfer, Bluetooth file transfer, or other apps that send the JPEG as an original file."
                )

                // Warns the user about apps that may recompress or modify the image.
                InfoSection(
                    theme = theme,
                    title = "Mediums That May Break Extraction",
                    iconType = "warning",
                    cardColor = cardColor,
                    borderColor = borderColor,
                    titleColor = titleColor,
                    bodyColor = bodyColor,
                    iconBg = iconBg,
                    iconTint = iconTint,
                    body = "Some apps recompress images when they are sent normally. This can change the JPEG data and destroy the hidden message. Apps like Discord, SMS/MMS, and normal social media image uploads may fail depending on the way the image is recompressed."
                )

                // Clarifies that transfer damage can cause extraction failure.
                InfoSection(
                    theme = theme,
                    title = "Important Reminder",
                    iconType = "warning",
                    cardColor = cardColor,
                    borderColor = borderColor,
                    titleColor = titleColor,
                    bodyColor = bodyColor,
                    iconBg = iconBg,
                    iconTint = iconTint,
                    body = "If extraction fails, it does not always mean the embed failed. It may mean the image was changed during transfer. Try sending the image again as an attachment or through a storage link."
                )

                // Small version label at the bottom of the page.
                Text(
                    text = "HIPS v1.0.0",
                    color = if (theme == AppTheme.DARK) Color(0xFF475569) else Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    theme: AppTheme,
    title: String,
    iconType: String,
    cardColor: Color,
    borderColor: Color,
    titleColor: Color,
    bodyColor: Color,
    iconBg: Color,
    iconTint: Color,
    body: String
) {
    // Chooses the icon based on the string passed into the section.
    // This keeps the InfoSection reusable instead of making separate card functions.
    val icon = when (iconType) {
        "mail" -> Icons.Default.Mail
        "image" -> Icons.Default.Image
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }

    // Reusable card layout for each documentation section.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon box on the left side of the card.
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Text content for the section.
        Column {
            Text(
                text = title,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                text = body,
                color = bodyColor,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

