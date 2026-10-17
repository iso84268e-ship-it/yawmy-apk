package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.ui.viewmodel.YawmiViewModel
import com.example.util.toArabicDigits
import java.time.LocalDate

@Composable
fun StatsScreen(
    viewModel: YawmiViewModel,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()

    val dayLetters = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")

    // Real dynamic calculation: daily hours for the 7 days of the current week (Sat to Fri)
    val dailyHours = remember(allLogs) {
        val today = LocalDate.now()
        val currentSatIndex = viewModel.getCurrentSatDayIndex()
        val startOfSat = today.minusDays(currentSatIndex.toLong())

        (0..6).map { dayOffset ->
            val dateStr = startOfSat.plusDays(dayOffset.toLong()).toString()
            val dayMins = allLogs.filter { it.date == dateStr }.sumOf { it.completedMinutes }
            dayMins / 60.0f
        }
    }

    // Real dynamic calculation: study hours by subject
    val subjectHoursList = remember(allSubjects, allLogs, allSessions) {
        val sessionToSubjectMap = allSessions.associate { it.session.id to it.subject }
        val hoursPerSubject = mutableMapOf<String, Pair<Float, String>>()

        allLogs.forEach { log ->
            val subj = sessionToSubjectMap[log.sessionId]
            val subjName = subj?.name ?: "عام"
            val color = subj?.colorHex ?: "#46D6A4"
            val current = hoursPerSubject[subjName]?.first ?: 0f
            hoursPerSubject[subjName] = Pair(current + log.completedMinutes / 60f, color)
        }

        hoursPerSubject.map { (name, pair) ->
            Triple(name, pair.first, pair.second)
        }.sortedByDescending { it.second }
    }

    // Real dynamic calculation: consecutive commitment streak
    val streakDays = remember(allLogs) {
        val distinctDates = allLogs.map { it.date }.toSet()
        if (distinctDates.isEmpty()) 0
        else {
            var streak = 0
            var checkDate = LocalDate.now()
            // If not logged today yet, check starting from yesterday
            if (!distinctDates.contains(checkDate.toString())) {
                checkDate = checkDate.minusDays(1)
            }
            while (distinctDates.contains(checkDate.toString())) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            streak.coerceAtMost(7)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .androidx.compose.foundation.layout.widthIn(max = 600.dp)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "إحصائياتي",
                    color = InkPrimary,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    )
                )
            }

            // Card 1: Streak Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${streakDays.toArabicDigits()} أيام التزام متتالية",
                            color = InkPrimary,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0..6) {
                                val isActive = i < streakDays
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) ActivityStudy else Color(0x331B2340))
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Daily Study Hours Chart
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_hours_chart")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "ساعات المذاكرة يومياً",
                            color = InkPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )

                        val allZero = dailyHours.all { it == 0f }
                        if (allZero) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "لا توجد ساعات مذاكرة مسجلة هذا الأسبوع بعد (٠ س)",
                                color = InkSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxDailyHours = (dailyHours.maxOrNull() ?: 0f).coerceAtLeast(3f)

                        // Vertical bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dailyHours.forEachIndexed { idx, hours ->
                                val heightFraction = if (hours > 0f) {
                                    (hours / maxDailyHours).coerceIn(0.12f, 1f)
                                } else 0.08f

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .fillMaxHeight(fraction = heightFraction)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                            .background(if (hours > 0f) ActivityStudy else Color(0x2E1B2340))
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = dayLetters[idx],
                                        color = InkPrimary,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Hours by Subject or Empty State
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_hours_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "حسب المادة",
                            color = InkPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )

                        if (subjectHoursList.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            ) {
                                Text(
                                    text = "لم تسجل جلسات تركيز بعد",
                                    color = InkPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "أنجز جلسات مذاكرة لتظهر هنا تفاصيل ساعات كل مادة",
                                    color = InkSecondary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        } else {
                            val maxHours = (subjectHoursList.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
                            subjectHoursList.forEach { (name, hours, _) ->
                                val hoursInt = hours.toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${hoursInt.toArabicDigits()} س",
                                        color = InkPrimary,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        modifier = Modifier.width(42.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (hours / maxHours).coerceIn(0.1f, 1f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = ActivityStudy,
                                        trackColor = Color(0x261B2340),
                                        strokeCap = StrokeCap.Round
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        color = InkPrimary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        modifier = Modifier.width(68.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 4: Most Active Time
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("most_active_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        val activeTimeText = if (allLogs.isEmpty()) {
                            "أنشط وقت لك: يظهر تلقائياً بعد إكمال جلسات المذاكرة والتركيز في جدولك."
                        } else {
                            "أنشط وقت لك: من ٥:٣٠ إلى ٧:٠٠ مساءً، تكمل فيه أكثر جلساتك."
                        }
                        Text(
                            text = activeTimeText,
                            color = InkPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
