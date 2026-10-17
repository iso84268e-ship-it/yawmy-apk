package com.example.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Session
import com.example.data.model.SessionType
import com.example.ui.components.ActivityChip
import com.example.ui.components.GlassCard
import com.example.ui.components.PrimaryInkButton
import com.example.ui.theme.ActivityStudy
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlassBorder70
import com.example.ui.theme.GlassWhite34
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.White
import com.example.ui.viewmodel.YawmiViewModel
import com.example.util.formatDurationHoursMinutes
import com.example.util.formatMinutesToTime

@Composable
fun SessionEditScreen(
    sessionId: Long?,
    viewModel: YawmiViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSessions by viewModel.allSessions.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()

    val existing = remember(allSessions, sessionId) {
        if (sessionId != null) allSessions.find { it.session.id == sessionId } else null
    }

    var selectedType by remember(existing) {
        mutableStateOf(existing?.session?.type ?: SessionType.STUDY)
    }
    var selectedSubjectId by remember(existing, allSubjects) {
        mutableStateOf(existing?.session?.subjectId ?: allSubjects.firstOrNull()?.id)
    }
    var topic by remember(existing) {
        mutableStateOf(existing?.session?.topic ?: "")
    }
    var startMinutes by remember(existing) {
        mutableIntStateOf(existing?.session?.startMinutes ?: (17 * 60 + 30)) // 5:30 PM
    }
    var endMinutes by remember(existing) {
        mutableIntStateOf(existing?.session?.endMinutes ?: (18 * 60 + 30))   // 6:30 PM
    }
    var repeatDaysMask by remember(existing) {
        mutableIntStateOf(existing?.session?.repeatDaysMask ?: 127) // All 7 days default
    }
    var reminderEnabled by remember(existing) {
        mutableStateOf(existing?.session?.reminderMinutes != 0)
    }

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    // Conflict check
    val conflict = remember(startMinutes, endMinutes, repeatDaysMask, sessionId, allSessions) {
        viewModel.checkConflict(startMinutes, endMinutes, repeatDaysMask, sessionId)
    }

    val dayLetters = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header with "×" and Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.35f))
                        .testTag("close_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = InkPrimary
                    )
                }

                Text(
                    text = if (sessionId != null) "تعديل الجلسة" else "جلسة جديدة",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        fontSize = 26.sp
                    )
                )

                if (existing != null) {
                    IconButton(
                        onClick = {
                            viewModel.deleteSession(existing.session)
                            onDismiss()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DangerRed.copy(alpha = 0.2f))
                            .testTag("delete_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "حذف",
                            tint = DangerRed
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        // 1. Session Type selection chips
        item {
            Column {
                Text(
                    text = "النوع",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SessionType.entries.forEach { type ->
                        ActivityChip(
                            text = type.labelArabic,
                            color = type.color,
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Subject selection (if relevant)
        if (selectedType != SessionType.REMINDER) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المادة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                        )
                        TextButton(onClick = { showAddSubjectDialog = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(16.dp))
                                Text("إضافة مادة", style = MaterialTheme.typography.labelMedium.copy(color = InkPrimary))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val selectedSubject = allSubjects.find { it.id == selectedSubjectId }

                    Box {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { showSubjectDropdown = true }
                                .testTag("subject_dropdown"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(selectedType.color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = selectedSubject?.name ?: "اختر المادة",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = InkPrimary
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = InkPrimary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false },
                            modifier = Modifier.background(White)
                        ) {
                            allSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.name, color = InkPrimary, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedSubjectId = subj.id
                                        showSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Topic input
        item {
            Column {
                Text(
                    text = "الموضوع",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    placeholder = { Text("مثلاً: الباب الثاني: الاتزان الكيميائي", color = InkSecondary.copy(alpha = 0.6f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassWhite34,
                        unfocusedContainerColor = GlassWhite34,
                        focusedBorderColor = InkPrimary,
                        unfocusedBorderColor = GlassBorder70,
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_input")
                )
            }
        }

        // 4. Time selection: Start & End pickers
        item {
            Column {
                Text(
                    text = "الوقت",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start time picker
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        startMinutes = hour * 60 + minute
                                        if (endMinutes <= startMinutes) {
                                            endMinutes = (startMinutes + 60).coerceAtMost(1439)
                                        }
                                    },
                                    startMinutes / 60,
                                    startMinutes % 60,
                                    false
                                ).show()
                            }
                            .testTag("start_time_picker"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("من", style = MaterialTheme.typography.bodySmall.copy(color = InkSecondary))
                            Text(
                                text = formatMinutesToTime(startMinutes),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                        }
                    }

                    // End time picker
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        endMinutes = hour * 60 + minute
                                    },
                                    endMinutes / 60,
                                    endMinutes % 60,
                                    false
                                ).show()
                            }
                            .testTag("end_time_picker"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إلى", style = MaterialTheme.typography.bodySmall.copy(color = InkSecondary))
                            Text(
                                text = formatMinutesToTime(endMinutes),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Conflict / Duration indicator line
                val durationMins = (endMinutes - startMinutes).coerceAtLeast(0)
                val durationText = "المدة: ${formatDurationHoursMinutes(durationMins)}"
                val conflictText = if (conflict.hasConflict) {
                    " · ⚠️ تعارض مع: ${conflict.conflictingSessionTitle}"
                } else {
                    " · لا يوجد تعارض مع جدولك"
                }

                Text(
                    text = durationText + conflictText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (conflict.hasConflict) DangerRed else InkSecondary,
                        fontWeight = if (conflict.hasConflict) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // 5. Weekly Repeat Days (س, ح, ن, ث, ر, خ, ج)
        item {
            Column {
                Text(
                    text = "تكرار الأسبوعي",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLetters.forEachIndexed { index, letter ->
                        val isDaySelected = (repeatDaysMask and (1 shl index)) != 0
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) ActivityStudy else White.copy(alpha = 0.4f))
                                .clickable {
                                    repeatDaysMask = repeatDaysMask xor (1 shl index)
                                }
                                .testTag("repeat_day_$index")
                        ) {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Reminder Switch
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نبّهني قبلها بـ ١٠ دقائق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = ActivityStudy,
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = White.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("reminder_switch")
                    )
                }
            }
        }

        // 7. Save in Schedule Button
        item {
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryInkButton(
                text = "حفظ في الجدول",
                onClick = {
                    val sessionToSave = Session(
                        id = sessionId ?: 0L,
                        type = selectedType,
                        subjectId = if (selectedType == SessionType.REMINDER) null else selectedSubjectId,
                        topic = topic.trim(),
                        startMinutes = startMinutes,
                        endMinutes = endMinutes,
                        repeatDaysMask = repeatDaysMask,
                        reminderMinutes = if (reminderEnabled) 10 else 0,
                        enabled = true
                    )
                    viewModel.saveSession(sessionToSave, onSuccess = onDismiss)
                },
                modifier = Modifier.testTag("save_session_button")
            )
        }
    }

    // Add subject dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("إضافة مادة دراسية جديدة", fontWeight = FontWeight.Bold, color = InkPrimary) },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    placeholder = { Text("اسم المادة") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            viewModel.addSubject(newSubjectName.trim())
                            newSubjectName = ""
                            showAddSubjectDialog = false
                        }
                    }
                ) {
                    Text("إضافة", fontWeight = FontWeight.Bold, color = InkPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("إلغاء", color = InkSecondary)
                }
            }
        )
    }
}
