package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SessionWithSubject
import com.example.data.model.SessionType
import com.example.ui.components.GlassCard
import com.example.ui.components.InkGlassCard
import com.example.ui.theme.ActivityLesson
import com.example.ui.theme.ActivitySchool
import com.example.ui.theme.ActivityStudy
import com.example.ui.theme.ActivityWakeup
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.White
import com.example.ui.theme.White70
import com.example.ui.viewmodel.YawmiViewModel
import com.example.util.formatMinutesToTime
import com.example.util.toArabicDigits
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleWeekScreen(
    viewModel: YawmiViewModel,
    onNavigateToEditSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val weeklyTarget by viewModel.weeklyTargetHours.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()

    val currentSatIndex = remember { viewModel.getCurrentSatDayIndex() }
    var selectedDayIndex by remember { mutableIntStateOf(currentSatIndex) }

    val daysOfWeekArabic = listOf(
        "السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"
    )

    // Calculate total completed study hours this week from real logs
    val totalStudyHoursThisWeek = remember(allLogs) {
        val totalMins = allLogs.sumOf { it.completedMinutes }
        totalMins / 60
    }

    val selectedDaySessions = remember(allSessions, selectedDayIndex) {
        allSessions.filter { item ->
            item.session.enabled && ((item.session.repeatDaysMask and (1 shl selectedDayIndex)) != 0)
        }.sortedBy { it.session.startMinutes }
    }

    // Date range header (e.g. "١٧ – ٢٣ أكتوبر")
    val dateRangeHeader = remember {
        val today = LocalDate.now()
        val satDate = today.minusDays(currentSatIndex.toLong())
        val friDate = satDate.plusDays(6)
        val monthName = satDate.month.getDisplayName(TextStyle.FULL, Locale("ar"))
        "${satDate.dayOfMonth.toArabicDigits()} – ${friDate.dayOfMonth.toArabicDigits()} $monthName"
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
        // Top Header
        item {
            Column {
                Text(
                    text = dateRangeHeader,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkSecondary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "أسبوعك",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 38.sp,
                        color = InkPrimary
                    )
                )
            }
        }

        // Target Card (Ink Glass Card)
        item {
            InkGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_target_card")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "ساعات المذاكرة هذا الأسبوع",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = White70,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${totalStudyHoursThisWeek.toArabicDigits()} من ${weeklyTarget.toArabicDigits()} ساعة",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val progress = (totalStudyHoursThisWeek.toFloat() / weeklyTarget.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ActivityStudy,
                        trackColor = White.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Week Visual Timeline Chart
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("week_timeline_chart")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Time scale header (6 ص, 12 ظ, 6 م, 11 م)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 56.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("٦ ص", style = MaterialTheme.typography.labelSmall.copy(color = InkSecondary))
                        Text("١٢ ظ", style = MaterialTheme.typography.labelSmall.copy(color = InkSecondary))
                        Text("٦ م", style = MaterialTheme.typography.labelSmall.copy(color = InkSecondary))
                        Text("١١ م", style = MaterialTheme.typography.labelSmall.copy(color = InkSecondary))
                    }

                    // 7 Rows: Saturday to Friday
                    daysOfWeekArabic.forEachIndexed { index, dayName ->
                        val isToday = index == currentSatIndex
                        val isSelected = index == selectedDayIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) White.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable { selectedDayIndex = index }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isToday) InkPrimary else InkSecondary,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.width(56.dp)
                            )

                            // Visual bar representing day from 6 AM (360m) to 11 PM (1380m) = 1020 mins span
                            val daySessions = allSessions.filter { item ->
                                item.session.enabled && ((item.session.repeatDaysMask and (1 shl index)) != 0)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(White.copy(alpha = 0.45f))
                            ) {
                                daySessions.forEach { item ->
                                    val start = (item.session.startMinutes - 360).coerceIn(0, 1020)
                                    val end = (item.session.endMinutes - 360).coerceIn(0, 1020)
                                    val startFraction = start.toFloat() / 1020f
                                    val widthFraction = ((end - start).toFloat() / 1020f).coerceAtLeast(0.04f)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = (startFraction + widthFraction).coerceAtMost(1f))
                                            .padding(start = (startFraction * 200).dp) // visual placement
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(item.session.type.color)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("استيقاظ", ActivityWakeup)
                        LegendItem("مدرسة", ActivitySchool)
                        LegendItem("دروس", ActivityLesson)
                        LegendItem("مذاكرة", ActivityStudy)
                    }
                }
            }
        }

        // Sessions List for Selected Day
        item {
            Text(
                text = "مواعيد يوم ${daysOfWeekArabic[selectedDayIndex]} (${selectedDaySessions.size.toArabicDigits()})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (selectedDaySessions.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد مواعيد مجدولة لهذا اليوم",
                            style = MaterialTheme.typography.bodyMedium.copy(color = InkSecondary)
                        )
                    }
                }
            }
        } else {
            items(selectedDaySessions, key = { it.session.id }) { item ->
                val s = item.session
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEditSession(s.id) },
                    backgroundColor = s.type.color.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val title = item.subject?.name ?: s.topic.ifBlank { s.type.labelArabic }
                            Text(
                                text = "${s.type.labelArabic}: $title",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                            if (s.topic.isNotBlank() && item.subject != null) {
                                Text(
                                    text = s.topic,
                                    style = MaterialTheme.typography.bodySmall.copy(color = InkSecondary)
                                )
                            }
                        }
                        Text(
                            text = "${formatMinutesToTime(s.startMinutes)} – ${formatMinutesToTime(s.endMinutes)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
