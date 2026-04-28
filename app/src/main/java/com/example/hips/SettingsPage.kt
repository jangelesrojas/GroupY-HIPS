package com.example.hips

// This screen contains the user settings for theme, reset, authentication changes, and app info.


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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// App theme options used by screens that support light and dark mode.
enum class AppTheme {
    LIGHT, DARK
}

// Data model for one row in the settings page.
data class SettingsItem(
    val label: String,
    val description: String,
    val danger: Boolean = false
)

// Groups settings rows into sections like Security, Appearance, and About.
data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

@Composable
fun SettingsPage(
    onBack: () -> Unit,
    theme: AppTheme,
    onToggleTheme: () -> Unit,
    onResetApp: () -> Unit,
    onChangePinGesture: () -> Unit,
    onOpenAppInfo: () -> Unit
) {
    // Controls the small reset confirmation popup.
    var showResetPopup by remember { mutableStateOf(false) }

    if (showResetPopup) {
        LaunchedEffect(showResetPopup) {
            delay(1600)
            showResetPopup = false
        }
    }

    // Settings list shown on the page.
    val settingsSections = listOf(
        SettingsSection(
            title = "Security",
            items = listOf(
                SettingsItem("Change PIN/Gesture", "Update your unlock method")
            )
        ),
        SettingsSection(
            title = "Appearance",
            items = listOf(
                SettingsItem("Theme", "Customize app appearance")
            )
        ),
        SettingsSection(
            title = "About",
            items = listOf(
                SettingsItem("App Info", "Version and documentation"),
                SettingsItem("Reset App", "Restore dark theme and default password", danger = true)
            )
        )
    )

    // Theme-aware colors used by the settings screen.
    val backgroundColor = if (theme == AppTheme.DARK) Color(0xFF0A0A0F) else Color(0xFFF9FAFB)
    val headerBorderColor = if (theme == AppTheme.DARK) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFE5E7EB)
    val titleColor = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827)
    val subtitleColor = if (theme == AppTheme.DARK) Color(0xFF64748B) else Color(0xFF6B7280)
    val cardColor = if (theme == AppTheme.DARK) Color(0xFF0F172A).copy(alpha = 0.5f) else Color.White
    val normalBorderColor = if (theme == AppTheme.DARK) Color(0xFF1E293B) else Color(0xFFE5E7EB)
    val hoverTextColor = if (theme == AppTheme.DARK) Color(0xFF94A3B8) else Color(0xFF9CA3AF)
    val violetIconBg = if (theme == AppTheme.DARK) Color(0xFF7C3AED).copy(alpha = 0.10f) else Color(0xFFEDE9FE)
    val violetIconTint = if (theme == AppTheme.DARK) Color(0xFFC4B5FD) else Color(0xFF7C3AED)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .border(0.5.dp, headerBorderColor)
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column {
                    Text(
                        text = "Settings",
                        color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure your preferences",
                        color = subtitleColor,
                        fontSize = 12.sp
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                settingsSections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = section.title.uppercase(),
                            color = subtitleColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            section.items.forEach { item ->
                                when (item.label) {
                                    "Theme" -> {
                                        ThemeSettingCard(
                                            theme = theme,
                                            cardColor = cardColor,
                                            borderColor = normalBorderColor,
                                            subtitleColor = subtitleColor,
                                            titleColor = titleColor,
                                            iconBg = violetIconBg,
                                            iconTint = violetIconTint,
                                            trailingColor = hoverTextColor,
                                            onClick = onToggleTheme
                                        )
                                    }

                                    "Change PIN/Gesture" -> {
                                        SettingsCard(
                                            label = item.label,
                                            description = item.description,
                                            iconType = "lock",
                                            danger = false,
                                            theme = theme,
                                            cardColor = cardColor,
                                            borderColor = normalBorderColor,
                                            onClick = onChangePinGesture
                                        )
                                    }

                                    "App Info" -> {
                                        SettingsCard(
                                            label = item.label,
                                            description = item.description,
                                            iconType = "info",
                                            danger = false,
                                            theme = theme,
                                            cardColor = cardColor,
                                            borderColor = normalBorderColor,
                                            onClick = onOpenAppInfo
                                        )
                                    }

                                    "Reset App" -> {
                                        SettingsCard(
                                            label = item.label,
                                            description = item.description,
                                            iconType = "delete",
                                            danger = true,
                                            theme = theme,
                                            cardColor = cardColor,
                                            borderColor = if (theme == AppTheme.DARK) {
                                                Color(0xFF7F1D1D).copy(alpha = 0.3f)
                                            } else {
                                                Color(0xFFFECACA)
                                            },
                                            onClick = {
                                                onResetApp()
                                                showResetPopup = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HIPS v1.0.0",
                        color = if (theme == AppTheme.DARK) Color(0xFF475569) else Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Steganographic Suite",
                        color = if (theme == AppTheme.DARK) Color(0xFF334155) else Color(0xFF6B7280),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (showResetPopup) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (theme == AppTheme.DARK) Color(0xFF111827) else Color.White)
                        .border(
                            1.dp,
                            if (theme == AppTheme.DARK) Color(0xFF334155) else Color(0xFFD1D5DB),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "App has been reset",
                        color = if (theme == AppTheme.DARK) Color.White else Color(0xFF111827),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeSettingCard(
    theme: AppTheme,
    cardColor: Color,
    borderColor: Color,
    subtitleColor: Color,
    titleColor: Color,
    iconBg: Color,
    iconTint: Color,
    trailingColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (theme == AppTheme.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = "Theme",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Theme",
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (theme == AppTheme.DARK) "Switch to light mode" else "Switch to dark mode",
                color = subtitleColor,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = trailingColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingsCard(
    label: String,
    description: String,
    iconType: String,
    danger: Boolean,
    theme: AppTheme,
    cardColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    val titleColor = when {
        danger && theme == AppTheme.DARK -> Color(0xFFFCA5A5)
        danger && theme == AppTheme.LIGHT -> Color(0xFFB91C1C)
        theme == AppTheme.DARK -> Color.White
        else -> Color(0xFF111827)
    }

    val descriptionColor = if (theme == AppTheme.DARK) Color(0xFF64748B) else Color(0xFF6B7280)

    val iconBg = when {
        danger && theme == AppTheme.DARK -> Color(0xFF7F1D1D).copy(alpha = 0.18f)
        danger && theme == AppTheme.LIGHT -> Color(0xFFFEE2E2)
        theme == AppTheme.DARK -> Color(0xFF1E293B).copy(alpha = 0.65f)
        else -> Color(0xFFF3F4F6)
    }

    val iconTint = when {
        danger && theme == AppTheme.DARK -> Color(0xFFFCA5A5)
        danger && theme == AppTheme.LIGHT -> Color(0xFFDC2626)
        theme == AppTheme.DARK -> Color(0xFFCBD5E1)
        else -> Color(0xFF374151)
    }

    val icon = when (iconType) {
        "lock" -> Icons.Default.Lock
        "info" -> Icons.Default.Info
        "delete" -> Icons.Default.Delete
        else -> Icons.Default.Info
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = descriptionColor,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (theme == AppTheme.DARK) Color(0xFF94A3B8) else Color(0xFF9CA3AF),
            modifier = Modifier.size(18.dp)
        )
    }
}

