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
    selectedImageName: String? = null,
    onSelectImageClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val hasImage = selectedImageName != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020611))
            .padding(24.dp)
    ) {
        // Begin HEADER section
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF0B2B2E), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF00B8B8), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Header icon
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = "Eye icon",
                    tint = Color(0xFF00D1D1),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                // Page title
                Text(
                    text = "Extract Message",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Page subtitle
                Text(
                    text = "Reveal a hidden message from an image",
                    color = Color(0xFF667799),
                    fontSize = 13.sp
                )
            }
        }
        // End of HEADER section

        Spacer(modifier = Modifier.height(28.dp))

        // Begin STEP INDICATOR section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Begin STEP 1
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
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Image",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            // End STEP 1

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFF2A3B5C))
            )

            // Begin STEP 2
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, Color(0xFF2A3B5C), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        color = Color(0xFF6E7F9F),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Confirm",
                    color = Color(0xFF6E7F9F),
                    fontSize = 12.sp
                )
            }
            // End STEP 2

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFF2A3B5C))
            )

            // Begin STEP 3
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, Color(0xFF2A3B5C), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        color = Color(0xFF6E7F9F),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Reveal",
                    color = Color(0xFF6E7F9F),
                    fontSize = 12.sp
                )
            }
            // End STEP 3
        }
        // End of STEP INDICATOR section

        Spacer(modifier = Modifier.height(30.dp))

        // Begin DESCRIPTION section
        Text(
            text = "Select a PNG carrier image that was created by HIPS.",
            color = Color(0xFFD7E3FF),
            fontSize = 16.sp
        )
        // End of DESCRIPTION section

        Spacer(modifier = Modifier.height(20.dp))

        // Begin IMAGE SELECT section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF3A537C),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(Color(0xFF030C22), RoundedCornerShape(20.dp))
                .clickable { onSelectImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image icon container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF1F2D46), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Image icon
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Image icon",
                        tint = Color(0xFF6E7F9F),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Selected image text
                Text(
                    text = if (selectedImageName == null) "Select Image" else selectedImageName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Helper text
                Text(
                    text = if (selectedImageName == null) "Tap to open gallery" else "Tap to change image",
                    color = Color(0xFF607299),
                    fontSize = 15.sp
                )
            }
        }
        // End of IMAGE SELECT section

        Spacer(modifier = Modifier.height(20.dp))

        // Begin CONTINUE BUTTON section
        Button(
            onClick = onContinueClick,
            enabled = hasImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A2942),
                disabledContainerColor = Color(0xFF1A2942),
                contentColor = Color(0xFF91A4CA),
                disabledContentColor = Color(0xFF62708C)
            )
        ) {
            Text(
                text = "Continue →",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // End of CONTINUE BUTTON section
    }
}