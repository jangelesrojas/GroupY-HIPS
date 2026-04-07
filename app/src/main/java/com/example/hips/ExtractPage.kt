package com.example.hips

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
fun ExtractPage(
    theme: AppTheme,
    selectedImageUri: Uri? = null,
    onBack: () -> Unit = {},
    onSelectImageClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var decodedMessage by remember { mutableStateOf<String?>(null) }
    var isDecoding by remember { mutableStateOf(false) }
    var decodeFailed by remember { mutableStateOf(false) }

    val hasImage = selectedImageUri != null

    val backgroundColor = if (theme == AppTheme.DARK) Color(0xFF0D0D1A) else Color(0xFFF8FAFC)
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val subtitleColor = if (theme == AppTheme.DARK) Color(0xFF667799) else Color(0xFF6B7280)
    val bodyColor = if (theme == AppTheme.DARK) Color(0xFFD7E3FF) else Color(0xFF374151)
    val cardColor = if (theme == AppTheme.DARK) Color(0xFF030C22) else Color.White
    val borderColor = if (theme == AppTheme.DARK) Color(0xFF3A537C) else Color(0xFFD1D5DB)
    val stepLineColor = if (theme == AppTheme.DARK) Color(0xFF2A3B5C) else Color(0xFFD1D5DB)
    val inactiveStepColor = if (theme == AppTheme.DARK) Color(0xFF6E7F9F) else Color(0xFF9CA3AF)
    val iconBoxColor = if (theme == AppTheme.DARK) Color(0xFF0B2B2E) else Color(0xFFECFEFF)
    val imageIconBoxColor = if (theme == AppTheme.DARK) Color(0xFF1F2D46) else Color(0xFFF3F4F6)
    val helperTextColor = if (theme == AppTheme.DARK) Color(0xFF607299) else Color(0xFF6B7280)
    val accent = Color(0xFF7B4FE0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 16.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = titleColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back", color = titleColor, fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBoxColor, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF00B8B8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Visibility, contentDescription = null, tint = Color(0xFF00D1D1), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Extract Message", color = titleColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Reveal a hidden message from an image", color = subtitleColor, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Step indicators
        val currentStep = when {
            decodedMessage != null || decodeFailed -> 3
            hasImage -> 2
            else -> 1
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StepIndicator("1", "Image", currentStep >= 1, titleColor, stepLineColor, inactiveStepColor)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(stepLineColor))
            StepIndicator("2", "Confirm", currentStep >= 2, titleColor, stepLineColor, inactiveStepColor)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(stepLineColor))
            StepIndicator("3", "Reveal", currentStep >= 3, titleColor, stepLineColor, inactiveStepColor)
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (decodedMessage != null) {
            Text("Hidden Message Found:", color = Color(0xFF00FF7F), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF00FF7F), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(text = decodedMessage!!, color = titleColor, fontSize = 16.sp, lineHeight = 24.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    decodedMessage = null
                    decodeFailed = false
                    onBack()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text("Back to Home", color = Color.White)
            }
        } else if (decodeFailed) {
            Text("No Hidden Message Found", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "This image does not appear to contain a HIPS-encoded message. Make sure you are selecting a PNG image that was created by the Embed feature.",
                color = bodyColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    decodeFailed = false
                    onBack()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text("Back to Home", color = Color.White)
            }
        } else {
            Text("Select a PNG carrier image that was created by HIPS.", color = bodyColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(20.dp))

            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .clickable { onSelectImageClick() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .background(cardColor, RoundedCornerShape(20.dp))
                        .clickable { onSelectImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(imageIconBoxColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null, tint = inactiveStepColor, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Select Image", color = titleColor, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to open gallery", color = helperTextColor, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (selectedImageUri == null) return@Button
                    isDecoding = true
                    val bitmap = BitmapUtils.loadBitmapFromUri(context, selectedImageUri)
                    if (bitmap == null) {
                        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                        isDecoding = false
                        return@Button
                    }
                    val pixels = BitmapUtils.bitmapToPixels(bitmap)
                    val result = Steganography.decodeMessage(pixels)
                    isDecoding = false
                    if (result != null && result.isNotEmpty()) {
                        decodedMessage = result
                    } else {
                        decodeFailed = true
                    }
                },
                enabled = hasImage && !isDecoding,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    disabledContainerColor = if (theme == AppTheme.DARK) Color(0xFF1A2942) else Color(0xFFE5E7EB)
                )
            ) {
                Text(
                    text = if (isDecoding) "Decoding..." else "Extract Message",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(number: String, label: String, active: Boolean, activeColor: Color, lineColor: Color, inactiveColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(if (active) 2.dp else 1.dp, if (active) Color(0xFF8B3DFF) else lineColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = if (active) activeColor else inactiveColor, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = if (active) activeColor else inactiveColor, fontSize = 12.sp)
    }
}
