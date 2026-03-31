package com.example.hips

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

// This controls the hidden taps needed to open the real app.
private const val SECRET_TAPS = 5
private const val SECRET_WINDOW_MS = 3000L

// These image links are reused by the fake journal cards.
private const val JOURNAL_IMAGE =
    "https://images.unsplash.com/photo-1700326276049-ffa3bd12d801?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb3JuaW5nJTIwam91cm5hbCUyMG5vdGVib29rJTIwY296eSUyMHdhcm0lMjBsaWdodHxlbnwxfHx8fDE3NzE4MTIxMjl8MA&ixlib=rb-4.1.0&q=80&w=1080"

private const val MINDFULNESS_IMAGE =
    "https://images.unsplash.com/photo-1758787412766-b63d89e31021?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwZWFjZWZ1bCUyMG1pbmRmdWxuZXNzJTIwbWVkaXRhdGlvbiUyMGdyZWVuJTIwcGxhbnRzfGVufDF8fHx8MTc3MTgxMjEzM3ww&ixlib=rb-4.1.0&q=80&w=1080"

// This stores one journal card item for the cover screen.
data class JournalEntry(
    val id: Int,
    val date: String,
    val mood: String,
    val title: String,
    val preview: String,
    val tags: List<String>,
    val image: String?
)

// This is sample content for the fake cover page.
private val entries = listOf(
    JournalEntry(
        id = 1,
        date = "Feb 22",
        mood = "great",
        title = "A fresh start",
        preview = "Had a wonderful morning. Made my favourite coffee, watched the sunrise from the balcony, and felt genuinely grateful for the small things.",
        tags = listOf("gratitude", "morning"),
        image = JOURNAL_IMAGE
    ),
    JournalEntry(
        id = 2,
        date = "Feb 21",
        mood = "okay",
        title = "Low energy, high reflection",
        preview = "Felt a bit flat today. Work was relentless but I still managed to meditate for 15 minutes before bed — even that helped.",
        tags = listOf("work", "mindfulness"),
        image = null
    ),
    JournalEntry(
        id = 3,
        date = "Feb 19",
        mood = "great",
        title = "Nature walk",
        preview = "Took the long route through the park. Leaves everywhere. Reminded me why getting outside matters.",
        tags = listOf("nature", "wellbeing"),
        image = MINDFULNESS_IMAGE
    )
)

@Composable
fun CoverAppScreen(
    onUnlock: () -> Unit
) {
    // This page stays the same and does not use the dark or light setting.
    val tapTimes = remember { mutableStateListOf<Long>() }
    var tapFeedback by remember { mutableStateOf(false) }
    var unlocked by remember { mutableStateOf(false) }

    val today = remember {
        SimpleDateFormat("EEEE d MMMM yyyy", Locale.UK).format(Date())
    }

    fun handleLogoTap() {
        if (unlocked) return

        val now = System.currentTimeMillis()

        val recent = tapTimes.filter { now - it < SECRET_WINDOW_MS }
        tapTimes.clear()
        tapTimes.addAll(recent)
        tapTimes.add(now)

        tapFeedback = true

        if (tapTimes.size >= SECRET_TAPS) {
            unlocked = true
        }
    }

    LaunchedEffect(tapFeedback) {
        if (tapFeedback) {
            delay(100)
            tapFeedback = false
        }
    }

    LaunchedEffect(unlocked) {
        if (unlocked) {
            delay(150)
            onUnlock()
            tapTimes.clear()
            unlocked = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF7ED)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { handleLogoTap() }
                        .scale(if (tapFeedback) 0.95f else 1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFBBF24)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Luminary",
                        color = Color(0xFF1F2937),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(color = Color(0xFFF3F4F6))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = today,
                                color = Color(0xFFD97706),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Good morning 👋",
                                    color = Color(0xFF1F2937),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.widthIn(max = 132.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "WE'RE IMPROVING",
                                        color = Color(0xFFD97706),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "New mood insights\ncoming soon",
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "How are you feeling today?",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFFBBF24), Color(0xFFFB923C))
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "QUOTE OF THE DAY",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "\"The present moment is the only moment available to us, and it is the door to all moments.\"",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "— Thich Nhat Hanh",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    StatCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                        value = "14",
                        label = "Day Streak"
                    )
                }

                item {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "Recent Reflections",
                            color = Color(0xFF374151),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(entries) { entry ->
                    ReflectionCard(entry = entry)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    // made this reusable card for showing quick stats like streaks and entries.
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color(0xFFF59E0B),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ReflectionCard(entry: JournalEntry) {
    // this card displays one journal/reflection entry.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (entry.image != null) {
                AsyncImage(
                    model = entry.image,
                    contentDescription = null,
                    modifier = Modifier
                        .width(80.dp)
                        .height(110.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.date,
                        color = Color(0xFFD97706),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row {
                        entry.tags.forEachIndexed { index, tag ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = Color(0xFFD97706),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = entry.title,
                    color = Color(0xFF1F2937),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = entry.preview,
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --Jose







