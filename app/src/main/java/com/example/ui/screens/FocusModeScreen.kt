package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.ActivityStudy
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.White
import com.example.ui.theme.White20
import com.example.ui.theme.White70
import com.example.ui.viewmodel.YawmiViewModel
import com.example.util.toArabicDigits
import java.util.Locale

@Composable
fun FocusModeScreen(
    sessionId: Long?,
    topicTitle: String?,
    viewModel: YawmiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pomodoroState by viewModel.pomodoroState.collectAsState()

    val mins = pomodoroState.remainingSeconds / 60
    val secs = pomodoroState.remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs).toArabicDigits()

    val elapsedSeconds = (pomodoroState.totalSeconds - pomodoroState.remainingSeconds).coerceAtLeast(0)
    val progress = if (pomodoroState.totalSeconds > 0) {
        (elapsedSeconds.toFloat() / pomodoroState.totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val titleDisplay = if (!topicTitle.isNullOrBlank()) topicTitle else pomodoroState.topicTitle

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header matching Screenshot 9
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.35f))
                        .testTag("focus_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = InkPrimary
                    )
                }

                Text(
                    text = "وضع التركيز",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = InkPrimary
                    )
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Mint Active Session Pill: "مذاكرة: الكيمياء · الباب الثاني"
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(ActivityStudy)
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .testTag("focus_topic_pill")
            ) {
                Text(
                    text = titleDisplay,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        // Circular Glass-Like Timer (Screenshot 9)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(290.dp)
                    .testTag("circular_focus_timer")
            ) {
                // Background circle filling the clock
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.32f))
                )

                Canvas(modifier = Modifier.size(270.dp)) {
                    // Full light translucent track
                    drawCircle(
                        color = Color(0x40FFFFFF),
                        radius = size.minDimension / 2 - 14.dp.toPx(),
                        style = Stroke(width = 16.dp.toPx())
                    )
                    // Dark navy progress sweep arc matching Screenshot 9
                    drawArc(
                        color = InkPrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * (if (progress <= 0f) 0.42f else progress),
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Large countdown text (e.g. ١٤:٢٢)
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 58.sp,
                            color = InkPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // "من ٢٥ دقيقة"
                    val totalMins = pomodoroState.totalSeconds / 60
                    Text(
                        text = "من ${totalMins.toArabicDigits()} دقيقة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Mode switch pills: "مذاكرة ٢٥" & "راحة ٥"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "مذاكرة ٢٥"
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!pomodoroState.isBreak) InkPrimary else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (!pomodoroState.isBreak) InkPrimary else White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setTimerMode(isBreak = false, durationMinutes = 25) }
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .testTag("mode_study_pill")
                ) {
                    Text(
                        text = "مذاكرة ٢٥",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!pomodoroState.isBreak) White else InkPrimary,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // "راحة ٥"
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (pomodoroState.isBreak) InkPrimary else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (pomodoroState.isBreak) InkPrimary else White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setTimerMode(isBreak = true, durationMinutes = 5) }
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .testTag("mode_break_pill")
                ) {
                    Text(
                        text = "راحة ٥",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (pomodoroState.isBreak) White else InkPrimary,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        // Cycle counter text: "الجلسة ٢ من ٤"
        item {
            Text(
                text = "الجلسة ${pomodoroState.currentSessionNumber.toArabicDigits()} من ${pomodoroState.totalSessionsInCycle.toArabicDigits()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    fontSize = 16.sp
                )
            )
        }

        // Control Buttons: Skip >|, Play/Pause ||, Stop ■
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button (>|)
                IconButton(
                    onClick = { viewModel.skipToNextSession() },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.45f))
                        .testTag("skip_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "تخطي الجلسة",
                        tint = InkPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(22.dp))

                // Play / Pause Button (Center big Ink button)
                IconButton(
                    onClick = { viewModel.togglePlayPauseTimer() },
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(InkPrimary)
                        .testTag("play_pause_timer_button")
                ) {
                    Icon(
                        imageVector = if (pomodoroState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (pomodoroState.isRunning) "إيقاف مؤقت" else "تشغيل",
                        tint = White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.width(22.dp))

                // Stop / Reset Button (■)
                IconButton(
                    onClick = { viewModel.stopTimer() },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.45f))
                        .testTag("stop_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "إيقاف وحفظ",
                        tint = InkPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Bottom Stats Glass Card (Screenshot 9)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_stats_card"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right column: "٥٠ د" / "تركيز اليوم"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${pomodoroState.todayFocusMinutes.toArabicDigits()} د",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = InkPrimary,
                                fontSize = 28.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تركيز اليوم",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = InkSecondary,
                                fontSize = 14.sp
                            )
                        )
                    }

                    // Vertical subtle divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(White.copy(alpha = 0.5f))
                    )

                    // Left column: "٢" / "جلسات مكتملة"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = pomodoroState.todayCompletedSessions.toArabicDigits(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = InkPrimary,
                                fontSize = 28.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "جلسات مكتملة",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = InkSecondary,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
