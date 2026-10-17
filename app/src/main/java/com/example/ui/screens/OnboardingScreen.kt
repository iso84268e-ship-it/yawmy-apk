package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.theme.ActivityStudy
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.White
import com.example.ui.viewmodel.YawmiViewModel
import com.example.util.formatMinutesToTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: YawmiViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Splash vs Questions view state
    var showSplash by remember { mutableStateOf(false) } // direct to page 8 or splash toggle

    // Times matching Screenshot 8
    var wakeUpMinutes by remember { mutableIntStateOf(6 * 60 + 30) }        // 6:30 ص
    var schoolDepartureMinutes by remember { mutableIntStateOf(7 * 60 + 15) } // 7:15 ص
    var schoolStartMinutes by remember { mutableIntStateOf(8 * 60) }         // 8:00 ص
    var schoolEndMinutes by remember { mutableIntStateOf(14 * 60) }          // 2:00 م
    var sleepMinutes by remember { mutableIntStateOf(22 * 60 + 30) }         // 10:30 م

    // Subjects matching Screenshot 8 - unselected by default
    val defaultAvailableSubjects = listOf("كيمياء", "فيزياء", "رياضيات", "عربي", "إنجليزي")
    val selectedSubjects = remember { mutableStateListOf<String>() }
    val allCustomSubjects = remember { mutableStateListOf<String>().apply { addAll(defaultAvailableSubjects) } }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Validation
    fun validateInputs(): Boolean {
        if (wakeUpMinutes >= schoolDepartureMinutes) {
            errorMessage = "وقت الاستيقاظ يجب أن يكون قبل موعد الخروج للمدرسة"
            return false
        }
        if (schoolDepartureMinutes > schoolStartMinutes) {
            errorMessage = "موعد الخروج يجب أن يكون قبل أو عند بداية اليوم الدراسي"
            return false
        }
        if (schoolStartMinutes >= schoolEndMinutes) {
            errorMessage = "وقت بداية المدرسة يجب أن يكون قبل وقت النهاية"
            return false
        }
        errorMessage = null
        return true
    }

    fun submitSchedule() {
        if (validateInputs()) {
            viewModel.completeOnboarding(
                wakeUpMinutes = wakeUpMinutes,
                schoolDepartureMinutes = schoolDepartureMinutes,
                schoolStartMinutes = schoolStartMinutes,
                schoolEndMinutes = schoolEndMinutes,
                sleepMinutes = sleepMinutes,
                selectedSubjectNames = selectedSubjects.toList()
            )
            onComplete()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step Progress Indicator Bar (Page 8 top)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(InkPrimary.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(InkPrimary)
                    )
                }
            }
        }

        // Header: Title "رتّب يومك" + Subtitle + App Icon in Corner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "رتّب يومك",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "حدّد مواعيدك الأساسية ونبني لك جدولاً أولياً",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = InkSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }

                // Small App Icon from Page 8
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(White.copy(alpha = 0.4f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_yawmi_logo),
                        contentDescription = "يومي",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Error message banner if any
        if (errorMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, DangerRed, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Field 1: "وقت الاستيقاظ" -> "٦:٣٠ ص"
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                wakeUpMinutes = hour * 60 + minute
                                errorMessage = null
                            },
                            wakeUpMinutes / 60,
                            wakeUpMinutes % 60,
                            false
                        ).show()
                    }
                    .testTag("onboarding_wakeup_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMinutesToTime(wakeUpMinutes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "وقت الاستيقاظ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Field 2: "موعد الخروج للمدرسة" -> "٧:١٥ ص"
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                schoolDepartureMinutes = hour * 60 + minute
                                errorMessage = null
                            },
                            schoolDepartureMinutes / 60,
                            schoolDepartureMinutes % 60,
                            false
                        ).show()
                    }
                    .testTag("onboarding_departure_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMinutesToTime(schoolDepartureMinutes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "موعد الخروج للمدرسة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Field 3: "المدرسة من" "٨:٠٠ ص" "إلى" "٢:٠٠ م"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "إلى" (End time)
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    schoolEndMinutes = hour * 60 + minute
                                    errorMessage = null
                                },
                                schoolEndMinutes / 60,
                                schoolEndMinutes % 60,
                                false
                            ).show()
                        }
                        .testTag("school_end_card"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMinutesToTime(schoolEndMinutes),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = InkPrimary
                            )
                        )
                        Text(
                            text = "إلى",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = InkSecondary
                            )
                        )
                    }
                }

                // "المدرسة من" (Start time)
                GlassCard(
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    schoolStartMinutes = hour * 60 + minute
                                    errorMessage = null
                                },
                                schoolStartMinutes / 60,
                                schoolStartMinutes % 60,
                                false
                            ).show()
                        }
                        .testTag("school_start_card"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMinutesToTime(schoolStartMinutes),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = InkPrimary
                            )
                        )
                        Text(
                            text = "المدرسة من",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                        )
                    }
                }
            }
        }

        // Field 4: "وقت النوم" -> "١٠:٣٠ م"
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                sleepMinutes = hour * 60 + minute
                                errorMessage = null
                            },
                            sleepMinutes / 60,
                            sleepMinutes % 60,
                            false
                        ).show()
                    }
                    .testTag("onboarding_sleep_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMinutesToTime(sleepMinutes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "وقت النوم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Section: "موادك" (Screenshot 8)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "موادك",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = InkPrimary,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allCustomSubjects.forEach { subj ->
                        val isSelected = selectedSubjects.contains(subj)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) InkPrimary else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) InkPrimary else White.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        if (selectedSubjects.size > 1) {
                                            selectedSubjects.remove(subj)
                                            errorMessage = null
                                        }
                                    } else {
                                        selectedSubjects.add(subj)
                                        errorMessage = null
                                    }
                                }
                                .padding(horizontal = 22.dp, vertical = 10.dp)
                                .testTag("subject_chip_$subj")
                        ) {
                            Text(
                                text = subj,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) White else InkPrimary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // "+ مادة" Pill
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(White.copy(alpha = 0.35f))
                            .border(1.dp, White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .clickable { showAddSubjectDialog = true }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .testTag("add_subject_pill")
                    ) {
                        Text(
                            text = "+ مادة",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons: "تخطي" and "التالي" (Screenshot 8)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "التالي" (Next) Primary Ink Button
                Button(
                    onClick = { submitSchedule() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkPrimary,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = "التالي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = White
                        )
                    )
                }

                // "تخطي" (Skip) Glass Button
                Button(
                    onClick = {
                        // Skip with defaults
                        viewModel.completeOnboarding(
                            wakeUpMinutes = 390,
                            schoolDepartureMinutes = 435,
                            schoolStartMinutes = 480,
                            schoolEndMinutes = 840,
                            sleepMinutes = 1350,
                            selectedSubjectNames = emptyList()
                        )
                        onComplete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White.copy(alpha = 0.35f),
                        contentColor = InkPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(54.dp)
                        .testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "تخطي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = InkPrimary
                        )
                    )
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("إضافة مادة جديدة", fontWeight = FontWeight.Bold, color = InkPrimary) },
            text = {
                OutlinedTextField(
                    value = newSubjectInput,
                    onValueChange = { newSubjectInput = it },
                    placeholder = { Text("مثلاً: تاريخ، جغرافيا") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newSubjectInput.trim()
                        if (trimmed.isNotBlank() && !allCustomSubjects.contains(trimmed)) {
                            allCustomSubjects.add(trimmed)
                            selectedSubjects.add(trimmed)
                            newSubjectInput = ""
                            showAddSubjectDialog = false
                            errorMessage = null
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
