package com.example.hips

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun ExtractPage(
    theme: AppTheme,
    selectedImageName: String? = null,
    onBack: () -> Unit = {},
    onSelectImageClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val hasImage = selectedImageName != null

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
    val buttonColor = if (theme == AppTheme.DARK) Color(0xFF1A2942) else Color(0xFFE5E7EB)
    val buttonContentColor = if (theme == AppTheme.DARK) Color(0xFF91A4CA) else Color(0xFF374151)
    val buttonDisabledColor = if (theme == AppTheme.DARK) Color(0xFF1A2942) else Color(0xFFE5E7EB)
    val buttonDisabledContentColor = if (theme == AppTheme.DARK) Color(0xFF62708C) else Color(0xFF9CA3AF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp)
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

        // Header section
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBoxColor, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF00B8B8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = "Eye icon",
                    tint = Color(0xFF00D1D1),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Extract Message",
                    color = titleColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Reveal a hidden message from an image",
                    color = subtitleColor,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Step indicator section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(2.dp, Color(0xFF8B3DFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = titleColor,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Image",
                    color = titleColor,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(stepLineColor)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, stepLineColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        color = inactiveStepColor,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Confirm",
                    color = inactiveStepColor,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(stepLineColor)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, stepLineColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        color = inactiveStepColor,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Reveal",
                    color = inactiveStepColor,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Description section
        Text(
            text = "Select a PNG carrier image that was created by HIPS.",
            color = bodyColor,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Image select section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(cardColor, RoundedCornerShape(20.dp))
                .clickable { onSelectImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(imageIconBoxColor, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Image icon",
                        tint = inactiveStepColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (selectedImageName == null) "Select Image" else selectedImageName,
                    color = titleColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (selectedImageName == null) "Tap to open gallery" else "Tap to change image",
                    color = helperTextColor,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Continue button section
        Button(
            onClick = onContinueClick,
            enabled = hasImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = buttonDisabledColor,
                contentColor = buttonContentColor,
                disabledContentColor = buttonDisabledContentColor
            )
        ) {
            Text(
                text = "Continue →",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}