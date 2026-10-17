package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SessionWithSubject
import com.example.data.model.SessionType
import com.example.ui.components.GlassCard
import com.example.ui.components.InkGlassCard
import com.example.ui.theme.ActivityStudy
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
fun TodayScreen(
    viewModel: YawmiViewModel,
    onNavigateToNewSession: (Long?) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToFocus: (Long?, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val currentMinutes by viewModel.currentTimeMinutes.collectAsState()
    val todaySatIndex = remember { viewModel.getCurrentSatDayIndex() }

    // Filter sessions active for today
    val todaySessions = remember(allSessions, todaySatIndex) {
        allSessions.filter { item ->
            item.session.enabled && ((item.session.repeatDaysMask and (1 shl todaySatIndex)) != 0)
        }.sortedBy { it.session.startMinutes }
    }

    // Identify current active session or next upcoming session
    val activeSession = remember(todaySessions, currentMinutes) {
        todaySessions.firstOrNull { item ->
            currentMinutes in item.session.startMinutes until item.session.endMinutes
        } ?: todaySessions.firstOrNull { it.session.startMinutes > currentMinutes }
        ?: todaySessions.firstOrNull()
    }

    // Formatted current Arabic date: e.g. "السبت · ١٧ أكتوبر"
    val arabicDateText = remember {
        val now = LocalDate.now()
        val dayName = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ar"))
        val dayNum = now.dayOfMonth.toArabicDigits()
        val monthName = now.month.getDisplayName(TextStyle.FULL, Locale("ar"))
        "$dayName · $dayNum $monthName"
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header: Date & Title & Action icons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = arabicDateText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = InkSecondary.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "يومك",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 38.sp,
                                color = InkPrimary
                            )
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val sId = activeSession?.session?.id
                                val title = activeSession?.subject?.name ?: activeSession?.session?.topic ?: "مذاكرة مركزة"
                                onNavigateToFocus(sId, title)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.35f))
                                .testTag("focus_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "وضع التركيز",
                                tint = InkPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToAbout,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.35f))
                                .testTag("about_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "عن التطبيق",
                                tint = InkPrimary
                            )
                        }
                    }
                }
            }

            // "الآن" (Now) Ink Glass Card - only shown if activeSession exists
            if (activeSession != null) {
                item {
                    val s = activeSession.session
                    val subjectName = activeSession.subject?.name ?: s.type.labelArabic
                    val totalDuration = (s.endMinutes - s.startMinutes).coerceAtLeast(1)
                    val elapsed = (currentMinutes - s.startMinutes).coerceIn(0, totalDuration)
                    val progress = elapsed.toFloat() / totalDuration.toFloat()
                    val remainingMins = (s.endMinutes - currentMinutes).coerceAtLeast(0)

                    InkGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToFocus(s.id, "$subjectName: ${s.topic}")
                            }
                            .testTag("now_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الآن",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = White70,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(s.type.color)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = s.type.labelArabic,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = InkPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (s.type == SessionType.STUDY) "مذاكرة $subjectName" else subjectName,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val timeRange = "من ${formatMinutesToTime(s.startMinutes)} إلى ${formatMinutesToTime(s.endMinutes)}"
                            Text(
                                text = "${s.topic} · $timeRange",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = White70,
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Smooth Mint Progress Bar
                            LinearProgressIndicator(
                                progress = { if (progress in 0.01f..0.99f) progress else 0.5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ActivityStudy,
                                trackColor = White.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round,
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (remainingMins > 0) "باقي ${remainingMins.toArabicDigits()} دقيقة" else "مكتمل",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )

                                Text(
                                    text = "ابدأ التركيز ⏱",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = ActivityStudy,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Timeline Items or Empty State
            if (todaySessions.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(ActivityStudy.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = InkPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "لا توجد مواعيد مجدولة لهذا اليوم",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "أضف جلستك أو درسك الأول لتنظيم يومك بهدوء",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = InkSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            androidx.compose.material3.Button(
                                onClick = { onNavigateToNewSession(null) },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = InkPrimary,
                                    contentColor = White
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة أول جلسة", fontWeight = FontWeight.Bold, color = White)
                            }
                        }
                    }
                }
            } else {
                items(todaySessions, key = { it.session.id }) { item ->
                    TodayTimelineCard(
                        sessionItem = item,
                        onClick = { onNavigateToNewSession(item.session.id) }
                    )
                }
            }
        }

        // Floating Action Button: "+ جلسة جديدة"
        FloatingActionButton(
            onClick = { onNavigateToNewSession(null) },
            containerColor = ActivityStudy,
            contentColor = InkPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 100.dp)
                .testTag("add_session_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "جلسة جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun TodayTimelineCard(
    sessionItem: SessionWithSubject,
    onClick: () -> Unit
) {
    val s = sessionItem.session
    val cardColor = s.type.color

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("timeline_item_${s.id}"),
        backgroundColor = cardColor.copy(alpha = 0.88f),
        borderColor = Color(0x66FFFFFF),
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content: Title & details
            Column(modifier = Modifier.weight(1f)) {
                val title = when (s.type) {
                    SessionType.STUDY -> "مذاكرة: ${sessionItem.subject?.name ?: s.topic}"
                    SessionType.LESSON -> "درس ${sessionItem.subject?.name ?: s.topic}"
                    SessionType.SCHOOL -> "المدرسة"
                    SessionType.REMINDER -> s.topic.ifBlank { "تذكير" }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
                if (s.topic.isNotBlank() && s.type != SessionType.REMINDER) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = s.topic,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right side: Time badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMinutesToTime(s.startMinutes).replace(" ", "\n"),
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                )
                if (s.endMinutes > s.startMinutes && (s.endMinutes - s.startMinutes) > 30) {
                    Text(
                        text = "حتى ${formatMinutesToTime(s.endMinutes)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
