package com.example.hips

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable

@Composable
fun RealMain(
    theme: AppTheme,
    onOpenSettings: () -> Unit,
    onOpenEmbed: () -> Unit,
    onOpenExtract: () -> Unit,
    onExit: () -> Unit
) {
    val backgroundColor = if (theme == AppTheme.DARK) Color(0xFF0D0D1A) else Color(0xFFF8FAFC)
    val surfaceColor = if (theme == AppTheme.DARK) Color(0xFF13132A) else Color.White
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val subtitleColor = if (theme == AppTheme.DARK) Color(0xFF888888) else Color(0xFF6B7280)
    val headerIconTint = if (theme == AppTheme.DARK) Color.White else Color(0xFF374151)
    val accentPurple = Color(0xFF7B4FE0)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 40.dp) // ← increased top padding
    ) {
        // Bar at top for spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (theme == AppTheme.DARK) Color(0xFF2D1B6B) else Color(0xFFEDE9FE), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF7B4FE0))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("HIPS", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("STEGANOGRAPHIC SUITE", color = accentPurple, fontSize = 10.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Settings icon
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = headerIconTint,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onOpenSettings() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                //Exit app icon
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Exit",
                    tint = headerIconTint,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onExit() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text that says secure session
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D2B1A), shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00FF7F), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Secure session active", color = Color(0xFF00FF7F), fontSize = 14.sp)
            }
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00FF7F), modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor, shape = RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "HIPS conceals secret messages inside ordinary image " +
                        "files using ordinary image files using " +
                        "Least Significant Bit steganography. The images " +
                        "appear completely normal to any observer.",
                color = titleColor, fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section that says mobile
        Text("MOBILE", color = subtitleColor, fontSize = 11.sp, letterSpacing = 2.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // Button for embed message
        FeatureCard(
            theme = theme,
            icon = Icons.Default.VisibilityOff,
            title = "Embed Message",
            subtitle = "Camera · Gallery · Confirm · Share",
            steps = listOf("Take photo", "Write message", "Confirm", "Share"),
            backgroundColor = Color(0xFF1E0A4A),
            onCardClick = { onOpenEmbed() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Button for Extract Message
        FeatureCard(
            theme = theme,
            icon = Icons.Default.Visibility,
            title = "Extract Message",
            subtitle = "Gallery · Confirm · Reveal · Done",
            steps = listOf("Select image", "Confirm", "Scan", "Done"),
            backgroundColor = Color(0xFF0A2A1E),
            onCardClick = { onOpenExtract() }
        )
    }
}

// FeatureCard function that is reused in Extract and Embed buttons
@Composable
fun FeatureCard(
    theme: AppTheme,
    icon: ImageVector,
    title: String,
    subtitle: String,
    steps: List<String>,
    backgroundColor: Color,
    onCardClick: () -> Unit = {}
) {
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val featureSubtitleColor = if (theme == AppTheme.DARK) Color(0xFFCBD5E1) else Color(0xFF6B7280)
    val chipColor = if (theme == AppTheme.DARK) Color(0xFF2A2A3A) else Color(0xFFE5E7EB)
    val chipTextColor = if (theme == AppTheme.DARK) Color(0xFFAAAAAA) else Color(0xFF4B5563)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(28.dp) // ← increased from 14.dp
            .clickable{onCardClick()}
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(88.dp) // ← increased from 44.dp
                    .background(backgroundColor.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF7B4FE0),
                    modifier = Modifier.size(48.dp) // ← increased from 24.dp
                )
            }
            Spacer(modifier = Modifier.width(16.dp)) // ← increased from 12.dp
            Column {
                Text(title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 22.sp) // ← increased from 16.sp
                Text(subtitle, color = featureSubtitleColor, fontSize = 15.sp) // ← increased from 11.sp
            }
        }
        Spacer(modifier = Modifier.height(20.dp)) // ← increased from 10.dp
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            steps.forEach { step ->
                Box(
                    modifier = Modifier
                        .background(chipColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = step,
                        color = chipTextColor,
                        fontSize = 14.sp,
                        maxLines = 1,          // ← forces single line
                        softWrap = false       // ← prevents wrapping
                    )
                }
            }
        }
    }
}

