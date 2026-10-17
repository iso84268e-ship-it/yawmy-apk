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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.NoteWithDetails
import com.example.data.model.Note
import com.example.data.model.NoteTask
import com.example.ui.components.ActivityChip
import com.example.ui.components.GlassCard
import com.example.ui.components.PrimaryInkButton
import com.example.ui.theme.ActivityLesson
import com.example.ui.theme.ActivityStudy
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GlassBorder70
import com.example.ui.theme.GlassWhite34
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.White
import com.example.ui.viewmodel.YawmiViewModel
import com.example.util.formatMinutesToTime
import com.example.util.toArabicDigits

@Composable
fun NotesScreen(
    viewModel: YawmiViewModel,
    modifier: Modifier = Modifier
) {
    val allNotes by viewModel.allNotes.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf<String?>(null) } // null = الكل
    var noteToEdit by remember { mutableStateOf<NoteWithDetails?>(null) }
    var isCreatingNewNote by remember { mutableStateOf(false) }

    // Filter notes
    val filteredNotes = remember(allNotes, searchQuery, selectedSubjectFilter) {
        allNotes.filter { noteDetails ->
            val matchSubject = selectedSubjectFilter == null || noteDetails.subject?.name == selectedSubjectFilter
            val matchQuery = searchQuery.isBlank() ||
                    noteDetails.note.title.contains(searchQuery, ignoreCase = true) ||
                    noteDetails.note.body.contains(searchQuery, ignoreCase = true) ||
                    noteDetails.tasks.any { it.text.contains(searchQuery, ignoreCase = true) }
            matchSubject && matchQuery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "الملاحظات",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 38.sp,
                        color = InkPrimary
                    )
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث في ملاحظاتك", color = InkSecondary.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "بحث",
                            tint = InkSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
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
                        .testTag("notes_search_input")
                )
            }

            // Subject Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActivityChip(
                            text = "الكل",
                            color = InkPrimary,
                            selected = selectedSubjectFilter == null,
                            onClick = { selectedSubjectFilter = null },
                            modifier = Modifier.testTag("filter_all")
                        )
                    }
                    items(allSubjects, key = { it.id }) { subject ->
                        val isSelected = selectedSubjectFilter == subject.name
                        val color = try {
                            Color(android.graphics.Color.parseColor(subject.colorHex))
                        } catch (_: Exception) {
                            ActivityStudy
                        }
                        ActivityChip(
                            text = subject.name,
                            color = color,
                            selected = isSelected,
                            onClick = { selectedSubjectFilter = subject.name },
                            modifier = Modifier.testTag("filter_subject_${subject.id}")
                        )
                    }
                }
            }

            // Empty state or Note Cards
            if (filteredNotes.isEmpty()) {
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
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = InkPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (allNotes.isEmpty()) "لا توجد ملاحظات بعد" else "لا توجد ملاحظات مطابقة",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (allNotes.isEmpty()) {
                                    "دوّن ملخصاتك وقوانين المواد وقوائم مهامك في مكان واحد"
                                } else {
                                    "جرّب البحث بكلمات أخرى أو اختر مادة أخرى"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = InkSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                            if (allNotes.isEmpty()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                androidx.compose.material3.Button(
                                    onClick = { isCreatingNewNote = true },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = InkPrimary,
                                        contentColor = White
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة أول ملاحظة", fontWeight = FontWeight.Bold, color = White)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredNotes, key = { it.note.id }) { item ->
                    NoteCardItem(
                        noteItem = item,
                        onTaskToggle = { task -> viewModel.toggleTaskDone(task) },
                        onClick = { noteToEdit = item }
                    )
                }
            }
        }

        // Floating Action Button: "+ ملاحظة جديدة"
        FloatingActionButton(
            onClick = { isCreatingNewNote = true },
            containerColor = InkPrimary,
            contentColor = White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 100.dp)
                .testTag("add_note_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ملاحظة جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
            }
        }
    }

    // Note Editor Dialog
    if (isCreatingNewNote || noteToEdit != null) {
        val editing = noteToEdit
        NoteEditorDialog(
            existingNote = editing,
            subjects = allSubjects,
            sessions = allSessions,
            onDismiss = {
                isCreatingNewNote = false
                noteToEdit = null
            },
            onSave = { note, tasks ->
                viewModel.saveNote(note, tasks) {
                    isCreatingNewNote = false
                    noteToEdit = null
                }
            },
            onDelete = { note ->
                viewModel.deleteNote(note)
                isCreatingNewNote = false
                noteToEdit = null
            }
        )
    }
}

@Composable
private fun NoteCardItem(
    noteItem: NoteWithDetails,
    onTaskToggle: (NoteTask) -> Unit,
    onClick: () -> Unit
) {
    val note = noteItem.note
    val subject = noteItem.subject
    val session = noteItem.session
    val tasks = noteItem.tasks

    val badgeColor = remember(subject) {
        if (subject != null) {
            try {
                Color(android.graphics.Color.parseColor(subject.colorHex))
            } catch (_: Exception) {
                ActivityStudy
            }
        } else ActivityLesson
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Subject badge
            if (subject != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Note title
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = InkPrimary,
                    fontSize = 20.sp
                )
            )

            // Note body
            if (note.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkSecondary,
                        lineHeight = 22.sp
                    )
                )
            }

            // Checklist tasks
            if (tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tasks.forEach { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onTaskToggle(task) }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (task.done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (task.done) ActivityStudy else InkSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (task.done) InkSecondary.copy(alpha = 0.7f) else InkPrimary,
                                    fontWeight = if (task.done) FontWeight.Normal else FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    val completedCount = tasks.count { it.done }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${completedCount.toArabicDigits()} من ${tasks.size.toArabicDigits()} مكتمل",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = InkSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Linked session footer
            if (session != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val timeStr = formatMinutesToTime(session.startMinutes)
                Text(
                    text = "مرتبطة بجلسة $timeStr",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = InkSecondary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    existingNote: NoteWithDetails?,
    subjects: List<com.example.data.model.Subject>,
    sessions: List<com.example.data.db.SessionWithSubject>,
    onDismiss: () -> Unit,
    onSave: (Note, List<NoteTask>) -> Unit,
    onDelete: (Note) -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.note?.title ?: "") }
    var body by remember { mutableStateOf(existingNote?.note?.body ?: "") }
    var selectedSubjectId by remember { mutableStateOf(existingNote?.note?.subjectId ?: subjects.firstOrNull()?.id) }
    var selectedSessionId by remember { mutableStateOf(existingNote?.note?.linkedSessionId) }

    val taskItems = remember {
        mutableStateListOf<Pair<String, Boolean>>().apply {
            existingNote?.tasks?.forEach { add(Pair(it.text, it.done)) }
        }
    }
    var newTaskText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                backgroundColor = White.copy(alpha = 0.95f)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Dialog Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "إلغاء", tint = InkPrimary)
                            }
                            Text(
                                text = if (existingNote != null) "تعديل الملاحظة" else "ملاحظة جديدة",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
                                )
                            )
                            if (existingNote != null) {
                                IconButton(onClick = { onDelete(existingNote.note) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = DangerRed)
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                    }

                    // Title
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("عنوان الملاحظة") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Body
                    item {
                        OutlinedTextField(
                            value = body,
                            onValueChange = { body = it },
                            label = { Text("نص الملاحظة أو القوانين والملخصات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Subject Chips
                    item {
                        Text("المادة", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subjects, key = { it.id }) { subj ->
                                val selected = selectedSubjectId == subj.id
                                val color = try {
                                    Color(android.graphics.Color.parseColor(subj.colorHex))
                                } catch (_: Exception) {
                                    ActivityStudy
                                }
                                ActivityChip(
                                    text = subj.name,
                                    color = color,
                                    selected = selected,
                                    onClick = { selectedSubjectId = subj.id }
                                )
                            }
                        }
                    }

                    // Checklist tasks section
                    item {
                        Text("قائمة المهام (Checklist)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newTaskText,
                                onValueChange = { newTaskText = it },
                                placeholder = { Text("إضافة عنصر للقائمة...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newTaskText.isNotBlank()) {
                                        taskItems.add(Pair(newTaskText.trim(), false))
                                        newTaskText = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "إضافة", tint = InkPrimary)
                            }
                        }
                    }

                    items(taskItems.size) { idx ->
                        val item = taskItems[idx]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (item.second) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (item.second) ActivityStudy else InkSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        taskItems[idx] = Pair(item.first, !item.second)
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.first,
                                style = MaterialTheme.typography.bodyMedium.copy(color = InkPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { taskItems.removeAt(idx) }) {
                                Icon(Icons.Filled.Close, contentDescription = "حذف العنصر", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Save Button
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        PrimaryInkButton(
                            text = "حفظ الملاحظة",
                            onClick = {
                                if (title.isNotBlank()) {
                                    val note = Note(
                                        id = existingNote?.note?.id ?: 0L,
                                        title = title.trim(),
                                        body = body.trim(),
                                        subjectId = selectedSubjectId,
                                        linkedSessionId = selectedSessionId,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    val tasks = taskItems.map {
                                        NoteTask(
                                            noteId = existingNote?.note?.id ?: 0L,
                                            text = it.first,
                                            done = it.second
                                        )
                                    }
                                    onSave(note, tasks)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
