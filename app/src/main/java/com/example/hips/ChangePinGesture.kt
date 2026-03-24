package com.example.hips

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
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

private enum class AuthMethodType {
    PIN, PATTERN
}

private enum class Step {
    CHOOSE_METHOD, VERIFY_CURRENT, ENTER_NEW, CONFIRM_NEW, SUCCESS
}

private data class DotPoint(
    val id: Int,
    val x: Float,
    val y: Float
)

private data class AuthData(
    val method: AuthMethodType,
    val pin: String,
    val pattern: List<Int>
)

@Composable
fun ChangePinGestureScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var targetMethod by remember { mutableStateOf(AuthMethodType.PIN) }
    var currentMethod by remember { mutableStateOf(AuthMethodType.PIN) }
    var step by remember { mutableStateOf(Step.CHOOSE_METHOD) }

    var currentValue by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var confirmValue by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val pattern = remember { mutableStateListOf<Int>() }
    var isDrawing by remember { mutableStateOf(false) }

    val dots = remember {
        val size = 3
        val spacing = 80f
        val offset = 40f
        List(size * size) { i ->
            DotPoint(
                id = i,
                x = offset + (i % size) * spacing,
                y = offset + (i / size) * spacing
            )
        }
    }

    fun getCurrentAuth(): AuthData {
        val prefs = context.getSharedPreferences("hips_auth", Context.MODE_PRIVATE)

        val savedMethod = prefs.getString("hips-auth-method", "pin") ?: "pin"
        val savedPin = prefs.getString("hips-pin", "1337") ?: "1337"
        val savedPattern = prefs.getString("hips-pattern", "0,1,2,4,8") ?: "0,1,2,4,8"

        return AuthData(
            method = if (savedMethod == "pattern") AuthMethodType.PATTERN else AuthMethodType.PIN,
            pin = savedPin,
            pattern = savedPattern.split(",").mapNotNull { it.toIntOrNull() }
        )
    }

    fun savePin(pin: String) {
        val prefs = context.getSharedPreferences("hips_auth", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("hips-pin", pin)
            .putString("hips-auth-method", "pin")
            .apply()
    }

    fun savePattern(patternValue: String) {
        val prefs = context.getSharedPreferences("hips_auth", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("hips-pattern", patternValue)
            .putString("hips-auth-method", "pattern")
            .apply()
    }

    fun clearPattern() {
        pattern.clear()
    }

    fun handleMethodSelect(selectedMethod: AuthMethodType) {
        val auth = getCurrentAuth()
        targetMethod = selectedMethod
        currentMethod = auth.method
        step = Step.VERIFY_CURRENT
        error = ""
        currentValue = ""
        newValue = ""
        confirmValue = ""
        clearPattern()
    }

    fun handlePinInput(digit: String) {
        when (step) {
            Step.VERIFY_CURRENT -> {
                val next = currentValue + digit
                currentValue = next

                if (next.length == 4) {
                    val auth = getCurrentAuth()
                    if (next == auth.pin) {
                        currentValue = ""
                        step = Step.ENTER_NEW
                        error = ""
                    } else {
                        error = "Incorrect current PIN"
                        scope.launch {
                            delay(1000)
                            currentValue = ""
                            error = ""
                        }
                    }
                }
            }

            Step.ENTER_NEW -> {
                val next = newValue + digit
                newValue = next

                if (next.length == 4) {
                    step = Step.CONFIRM_NEW
                }
            }

            Step.CONFIRM_NEW -> {
                val next = confirmValue + digit
                confirmValue = next

                if (next.length == 4) {
                    if (next == newValue) {
                        savePin(newValue)
                        step = Step.SUCCESS
                    } else {
                        error = "PINs do not match"
                        scope.launch {
                            delay(1000)
                            confirmValue = ""
                            newValue = ""
                            step = Step.ENTER_NEW
                            error = ""
                        }
                    }
                }
            }

            else -> {}
        }
    }

    fun handlePinDelete() {
        when (step) {
            Step.VERIFY_CURRENT -> currentValue = currentValue.dropLast(1)
            Step.ENTER_NEW -> newValue = newValue.dropLast(1)
            Step.CONFIRM_NEW -> confirmValue = confirmValue.dropLast(1)
            else -> {}
        }
    }

    fun handlePatternEnd() {
        isDrawing = false

        if (pattern.size < 4) {
            error = "Pattern must connect at least 4 dots"
            scope.launch {
                delay(1000)
                clearPattern()
                error = ""
            }
            return
        }

        when (step) {
            Step.VERIFY_CURRENT -> {
                val auth = getCurrentAuth()
                if (pattern.toList() == auth.pattern) {
                    clearPattern()
                    step = Step.ENTER_NEW
                    error = ""
                } else {
                    error = "Incorrect pattern"
                    scope.launch {
                        delay(1000)
                        clearPattern()
                        error = ""
                    }
                }
            }

            Step.ENTER_NEW -> {
                newValue = pattern.joinToString(",")
                clearPattern()
                step = Step.CONFIRM_NEW
            }

            Step.CONFIRM_NEW -> {
                if (pattern.joinToString(",") == newValue) {
                    savePattern(newValue)
                    step = Step.SUCCESS
                } else {
                    error = "Patterns do not match"
                    scope.launch {
                        delay(1000)
                        clearPattern()
                        newValue = ""
                        step = Step.ENTER_NEW
                        error = ""
                    }
                }
            }

            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0A0F)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Change PIN / Gesture",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Update your authentication method",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    step == Step.CHOOSE_METHOD -> {
                        ChooseMethodContent(
                            getCurrentMethod = { getCurrentAuth().method },
                            onSelectMethod = { handleMethodSelect(it) }
                        )
                    }

                    step == Step.VERIFY_CURRENT && currentMethod == AuthMethodType.PIN -> {
                        PinEntryContent(
                            step = step,
                            currentPin = currentValue,
                            newPin = newValue,
                            confirmPin = confirmValue,
                            error = error,
                            onDigit = { handlePinInput(it) },
                            onDelete = { handlePinDelete() }
                        )
                    }

                    step == Step.VERIFY_CURRENT && currentMethod == AuthMethodType.PATTERN -> {
                        PatternEntryContent(
                            step = step,
                            pattern = pattern,
                            error = error,
                            dots = dots,
                            isEnabled = true,
                            onPatternStart = { isDrawing = true },
                            onDotHit = { dotId ->
                                if (!pattern.contains(dotId)) {
                                    pattern.add(dotId)
                                }
                            },
                            onPatternEnd = { handlePatternEnd() }
                        )
                    }

                    step == Step.ENTER_NEW && targetMethod == AuthMethodType.PIN -> {
                        PinEntryContent(
                            step = step,
                            currentPin = currentValue,
                            newPin = newValue,
                            confirmPin = confirmValue,
                            error = error,
                            onDigit = { handlePinInput(it) },
                            onDelete = { handlePinDelete() }
                        )
                    }

                    step == Step.ENTER_NEW && targetMethod == AuthMethodType.PATTERN -> {
                        PatternEntryContent(
                            step = step,
                            pattern = pattern,
                            error = error,
                            dots = dots,
                            isEnabled = true,
                            onPatternStart = { isDrawing = true },
                            onDotHit = { dotId ->
                                if (!pattern.contains(dotId)) {
                                    pattern.add(dotId)
                                }
                            },
                            onPatternEnd = { handlePatternEnd() }
                        )
                    }

                    step == Step.CONFIRM_NEW && targetMethod == AuthMethodType.PIN -> {
                        PinEntryContent(
                            step = step,
                            currentPin = currentValue,
                            newPin = newValue,
                            confirmPin = confirmValue,
                            error = error,
                            onDigit = { handlePinInput(it) },
                            onDelete = { handlePinDelete() }
                        )
                    }

                    step == Step.CONFIRM_NEW && targetMethod == AuthMethodType.PATTERN -> {
                        PatternEntryContent(
                            step = step,
                            pattern = pattern,
                            error = error,
                            dots = dots,
                            isEnabled = true,
                            onPatternStart = { isDrawing = true },
                            onDotHit = { dotId ->
                                if (!pattern.contains(dotId)) {
                                    pattern.add(dotId)
                                }
                            },
                            onPatternEnd = { handlePatternEnd() }
                        )
                    }

                    step == Step.SUCCESS -> {
                        SuccessContent(
                            targetMethod = targetMethod,
                            onBack = onBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChooseMethodContent(
    getCurrentMethod: () -> AuthMethodType,
    onSelectMethod: (AuthMethodType) -> Unit
) {
    val currentMethod = getCurrentMethod()
    val currentMethodName = if (currentMethod == AuthMethodType.PIN) "4-Digit PIN" else "Pattern Lock"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Choose Authentication Method",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select how you want to unlock HIPS",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current: $currentMethodName",
                color = Color(0xFFA78BFA),
                style = MaterialTheme.typography.bodySmall
            )
        }

        MethodCard(
            title = "4-Digit PIN",
            subtitle = "Traditional numeric code",
            icon = Icons.Default.Lock,
            backgroundColor = Color(0x332E1065),
            iconBg = Color(0x335B21B6),
            iconTint = Color(0xFFA78BFA),
            isCurrent = currentMethod == AuthMethodType.PIN,
            onClick = { onSelectMethod(AuthMethodType.PIN) }
        )

        MethodCard(
            title = "Pattern Lock",
            subtitle = "Draw a pattern on 3×3 grid",
            icon = Icons.Default.GridView,
            backgroundColor = Color(0x33134E4A),
            iconBg = Color(0x332D7D77),
            iconTint = Color(0xFF5EEAD4),
            isCurrent = currentMethod == AuthMethodType.PATTERN,
            onClick = { onSelectMethod(AuthMethodType.PATTERN) }
        )
    }
}

@Composable
private fun MethodCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconBg: Color,
    iconTint: Color,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
            }

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .background(Color(0x1A10B981), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CURRENT",
                        color = Color(0xFF34D399),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PinEntryContent(
    step: Step,
    currentPin: String,
    newPin: String,
    confirmPin: String,
    error: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val currentValue = when (step) {
        Step.VERIFY_CURRENT -> currentPin
        Step.ENTER_NEW -> newPin
        Step.CONFIRM_NEW -> confirmPin
        else -> ""
    }

    val title = when (step) {
        Step.VERIFY_CURRENT -> "Enter Current PIN"
        Step.ENTER_NEW -> "Enter New PIN"
        Step.CONFIRM_NEW -> "Confirm New PIN"
        else -> ""
    }

    val subtitle = when (step) {
        Step.VERIFY_CURRENT -> "Verify your identity"
        Step.ENTER_NEW -> "Choose a new 4-digit code"
        Step.CONFIRM_NEW -> "Re-enter your new PIN"
        else -> ""
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = if (i < currentValue.length) Color(0xFF8B5CF6) else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (i < currentValue.length) Color(0xFF8B5CF6) else Color(0xFF475569),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = Color(0xFFF87171),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del")

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { key ->
                        when (key) {
                            "" -> Spacer(modifier = Modifier.size(72.dp))

                            "del" -> {
                                Button(
                                    onClick = onDelete,
                                    enabled = currentValue.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x991E293B)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFCBD5E1)
                                    )
                                }
                            }

                            else -> {
                                Button(
                                    onClick = { onDigit(key) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x991E293B)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Text(
                                        text = key,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternEntryContent(
    step: Step,
    pattern: List<Int>,
    error: String,
    dots: List<DotPoint>,
    isEnabled: Boolean,
    onPatternStart: () -> Unit,
    onDotHit: (Int) -> Unit,
    onPatternEnd: () -> Unit
) {
    val title = when (step) {
        Step.VERIFY_CURRENT -> "Draw Current Pattern"
        Step.ENTER_NEW -> "Draw New Pattern"
        Step.CONFIRM_NEW -> "Confirm New Pattern"
        else -> ""
    }

    val subtitle = when (step) {
        Step.VERIFY_CURRENT -> "Verify your identity"
        Step.ENTER_NEW -> "Connect at least 4 dots"
        Step.CONFIRM_NEW -> "Draw the same pattern again"
        else -> ""
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = Color(0xFFF87171),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PatternCanvas(
            selectedPattern = pattern,
            dots = dots,
            enabled = isEnabled,
            onDotHit = onDotHit,
            onStart = onPatternStart,
            onFinish = onPatternEnd
        )

        Spacer(modifier = Modifier.height(16.dp))

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
}

@Composable
private fun PatternCanvas(
    selectedPattern: List<Int>,
    dots: List<DotPoint>,
    enabled: Boolean,
    onDotHit: (Int) -> Unit,
    onStart: () -> Unit,
    onFinish: () -> Unit
) {
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
                            if (!enabled) return@detectDragGestures
                            onStart()

                            dots.forEach { dot ->
                                val dx = offset.x - dot.x
                                val dy = offset.y - dot.y
                                if (sqrt(dx * dx + dy * dy) < 20f) {
                                    onDotHit(dot.id)
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (!enabled) return@detectDragGestures

                            dots.forEach { dot ->
                                val dx = change.position.x - dot.x
                                val dy = change.position.y - dot.y
                                if (sqrt(dx * dx + dy * dy) < 20f) {
                                    onDotHit(dot.id)
                                }
                            }
                        },
                        onDragEnd = {
                            if (enabled) onFinish()
                        }
                    )
                }
        ) {
            if (selectedPattern.size > 1) {
                for (i in 0 until selectedPattern.size - 1) {
                    val start = dots[selectedPattern[i]]
                    val end = dots[selectedPattern[i + 1]]

                    drawLine(
                        color = Color(0xFF8B5CF6),
                        start = Offset(start.x, start.y),
                        end = Offset(end.x, end.y),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
            }

            dots.forEachIndexed { index, dot ->
                val selected = selectedPattern.contains(index)

                drawCircle(
                    color = if (selected) Color(0xFFA78BFA) else Color(0xFF475569),
                    radius = if (selected) 12f else 8f,
                    center = Offset(dot.x, dot.y)
                )

                if (selected) {
                    drawCircle(
                        color = Color(0xFF8B5CF6),
                        radius = 12f,
                        center = Offset(dot.x, dot.y),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    targetMethod: AuthMethodType,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0x3310B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color(0xFF34D399),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Success!",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your authentication method has been changed to ${
                if (targetMethod == AuthMethodType.PIN) "PIN" else "Pattern"
            }",
            color = Color(0xFF94A3B8),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Settings", color = Color.White)
        }
    }
}