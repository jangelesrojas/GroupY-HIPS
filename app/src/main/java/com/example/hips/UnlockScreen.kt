package com.example.hips

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
private const val PREFS_NAME = "hips_auth"
private const val DEFAULT_PIN = "1234"
private const val DEFAULT_PATTERN = "0,1,2,4,8"

enum class AuthMethod {
    PIN, PATTERN
}

@Composable
fun UnlockScreen(
    theme: AppTheme,
    onSuccess: () -> Unit,
    onBack: () -> Unit
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

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    val scope = rememberCoroutineScope()

    var authMethod by rememberSaveable { mutableStateOf(AuthMethod.PIN) }
    var pin by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf("") }
    var attempts by rememberSaveable { mutableIntStateOf(0) }
    var locked by rememberSaveable { mutableStateOf(false) }
    var lockCountdown by rememberSaveable { mutableIntStateOf(0) }

    val pattern = remember { mutableStateListOf<Int>() }
    var isDrawing by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val savedMethod = prefs.getString("hips-auth-method", "pin") ?: "pin"
        authMethod = if (savedMethod == "pattern") AuthMethod.PATTERN else AuthMethod.PIN
    }

    LaunchedEffect(locked, lockCountdown) {
        if (locked && lockCountdown > 0) {
            delay(1000)
            lockCountdown -= 1
        } else if (locked && lockCountdown == 0) {
            locked = false
            attempts = 0
            errorMsg = ""
        }
    }

    suspend fun triggerShake() {
        val sequence = listOf(-18f, 18f, -14f, 14f, -8f, 8f, 0f)
        sequence.forEach {
            shakeOffset.animateTo(it, animationSpec = tween(50))
        }
    }

    fun resetPattern() {
        pattern.clear()
        isDrawing = false
    }

    fun handleFailedAttempt(label: String) {
        attempts += 1
        pin = ""
        resetPattern()

        if (attempts >= 5) {
            locked = true
            lockCountdown = 30
            errorMsg = "Too many attempts. Locked for 30 seconds."
        } else {
            scope.launch { triggerShake() }
            errorMsg = if (attempts >= 3) {
                val remaining = 5 - attempts
                "Incorrect $label. $remaining attempt${if (remaining == 1) "" else "s"} remaining."
            } else {
                "Incorrect $label. Please try again."
            }
        }
    }

    fun handleDigit(digit: String) {
        if (locked || pin.length >= 4) return

        val next = pin + digit
        pin = next
        errorMsg = ""

        if (next.length == 4) {
            val savedPin = prefs.getString("hips-pin", DEFAULT_PIN) ?: DEFAULT_PIN
            if (next == savedPin) {
                onSuccess()
            } else {
                handleFailedAttempt("PIN")
            }
        }
    }

    fun handleDelete() {
        if (locked) return
        pin = pin.dropLast(1)
        errorMsg = ""
    }

    fun handlePatternFinish() {
        isDrawing = false

        if (pattern.size < 4) {
            errorMsg = "Pattern must connect at least 4 dots"
            scope.launch {
                delay(1000)
                resetPattern()
                errorMsg = ""
            }
            return
        }

        val savedPattern =
            prefs.getString("hips-pattern", DEFAULT_PATTERN) ?: DEFAULT_PATTERN
        val correctPattern = savedPattern.split(",").mapNotNull { it.toIntOrNull() }

        if (pattern.toList() == correctPattern) {
            onSuccess()
        } else {
            handleFailedAttempt("pattern")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", color = Color(0xFF94A3B8))
            }

            TextButton(
                onClick = {
                    authMethod = if (authMethod == AuthMethod.PIN) AuthMethod.PATTERN else AuthMethod.PIN
                    pin = ""
                    resetPattern()
                    errorMsg = ""
                    prefs.edit()
                        .putString("hips-auth-method", if (authMethod == AuthMethod.PIN) "pin" else "pattern")
                        .apply()
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                if (authMethod == AuthMethod.PIN) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Switch to pattern",
                        tint = Color(0xFF94A3B8)
                    )
                } else {
                    Text("#", color = Color(0xFF94A3B8), style = MaterialTheme.typography.titleMedium)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = shakeOffset.value.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Logo",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Luminary", color = Color(0xFFCBD5E1))
                Text(
                    "Account security check",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(36.dp))

                if (authMethod == AuthMethod.PIN) {
                    Text(
                        "Enter your PIN",
                        //color = primaryText,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (locked) {
                            "Locked — try again in ${lockCountdown}s"
                        } else {
                            "Enter your 4-digit access code"
                        },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (i < pin.length) Color(0xFF8B5CF6) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (i < pin.length) Color(0xFF8B5CF6) else Color(0xFF475569),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (errorMsg.isNotEmpty()) {
                            Text(
                                text = errorMsg,
                                color = Color(0xFFF87171),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del")

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.width(260.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(keys) { _, key ->
                            when (key) {
                                "" -> Box(modifier = Modifier.size(72.dp))
                                "del" -> {
                                    Button(
                                        onClick = { handleDelete() },
                                        enabled = !locked && pin.isNotEmpty(),
                                        modifier = Modifier.size(72.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1E293B),
                                            disabledContainerColor = Color(0xFF1E293B)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { handleDigit(key) },
                                        enabled = !locked,
                                        modifier = Modifier.size(72.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1E293B),
                                            disabledContainerColor = Color(0xFF1E293B)
                                        )
                                    ) {
                                        Text(
                                            key,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Draw your pattern",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (locked) {
                            "Locked — try again in ${lockCountdown}s"
                        } else {
                            "Connect the dots to unlock"
                        },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier.height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (errorMsg.isNotEmpty()) {
                            Text(
                                text = errorMsg,
                                color = Color(0xFFF87171),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PatternCanvas(
                        selectedPattern = pattern.toList(),
                        enabled = !locked,
                        onDotHit = { dotId ->
                            if (!pattern.contains(dotId)) {
                                pattern.add(dotId)
                                errorMsg = ""
                            }
                        },
                        onStart = {
                            if (!locked) {
                                resetPattern()
                                isDrawing = true
                            }
                        },
                        onFinish = {
                            if (!locked) {
                                handlePatternFinish()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (pattern.isNotEmpty()) {
                            "${pattern.size} dot${if (pattern.size > 1) "s" else ""} connected"
                        } else {
                            "Touch and drag to connect dots"
                        },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PatternCanvas(
    selectedPattern: List<Int>,
    enabled: Boolean,
    onDotHit: (Int) -> Unit,
    onStart: () -> Unit,
    onFinish: () -> Unit
) {
    val dotPositions = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = Modifier
            .size(280.dp)
            .background(
                color = Color(0x80111827),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF334155),
                shape = RoundedCornerShape(20.dp)
            )
            .alpha(if (enabled) 1f else 0.3f)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled, selectedPattern) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (enabled) {
                                onStart()
                                val hit = findHitDot(offset, dotPositions)
                                if (hit != null) {
                                    onDotHit(hit)
                                }
                            }
                        },
                        onDragEnd = {
                            if (enabled) {
                                onFinish()
                            }
                        },
                        onDragCancel = {
                            if (enabled) {
                                onFinish()
                            }
                        },
                        onDrag = { change, _ ->
                            if (enabled) {
                                val hit = findHitDot(change.position, dotPositions)
                                if (hit != null) {
                                    onDotHit(hit)
                                }
                            }
                        }
                    )
                }
                .pointerInput(enabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (enabled) {
                                onStart()
                                val hit = findHitDot(offset, dotPositions)
                                if (hit != null) {
                                    onDotHit(hit)
                                }
                                tryAwaitRelease()
                                onFinish()
                            }
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val spacingX = w / 4f
            val spacingY = h / 4f

            val positions = List(9) { i ->
                val col = i % 3
                val row = i / 3
                Offset(
                    x = spacingX * (col + 1),
                    y = spacingY * (row + 1)
                )
            }

            if (dotPositions.size != 9) {
                dotPositions.clear()
                dotPositions.addAll(positions)
            }

            if (selectedPattern.size > 1) {
                for (i in 0 until selectedPattern.lastIndex) {
                    val start = positions[selectedPattern[i]]
                    val end = positions[selectedPattern[i + 1]]

                    drawLine(
                        color = Color(0xFF8B5CF6),
                        start = start,
                        end = end,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                }
            }

            positions.forEachIndexed { index, pos ->
                val selected = selectedPattern.contains(index)

                drawCircle(
                    color = if (selected) Color(0xFFA78BFA) else Color(0xFF475569),
                    radius = if (selected) 18f else 12f,
                    center = pos
                )

                if (selected) {
                    drawCircle(
                        color = Color(0xFF8B5CF6),
                        radius = 18f,
                        center = pos,
                        style = Stroke(width = 4f)
                    )
                }
            }
        }
    }
}

private fun findHitDot(point: Offset, dots: List<Offset>): Int? {
    dots.forEachIndexed { index, dot ->
        val dx = point.x - dot.x
        val dy = point.y - dot.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance < 40f) {
            return index
        }
    }
    return null
}










