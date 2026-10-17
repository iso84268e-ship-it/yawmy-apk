package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ui.theme.ActivityLesson
import com.example.ui.theme.ActivitySchool
import com.example.ui.theme.ActivityStudy
import com.example.ui.theme.ActivityWakeup

enum class SessionType(val labelArabic: String, val color: Color, val hex: String) {
    STUDY("مذاكرة", ActivityStudy, "#46D6A4"),
    LESSON("درس", ActivityLesson, "#7CCBFF"),
    SCHOOL("مدرسة", ActivitySchool, "#FFE3A8"),
    REMINDER("تذكير", ActivityWakeup, "#CDB8FF");

    companion object {
        fun fromString(value: String): SessionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: STUDY
        }
    }
}

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#46D6A4"
)

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subjectId")]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: SessionType,
    val subjectId: Long? = null,
    val topic: String,
    val startMinutes: Int, // Minutes from 00:00 (e.g. 5:30 PM = 17*60+30 = 1050)
    val endMinutes: Int,   // Minutes from 00:00 (e.g. 6:30 PM = 18*60+30 = 1110)
    val repeatDaysMask: Int = 127, // Bits 0..6 for Saturday..Friday (127 = every day)
    val reminderMinutes: Int = 10,  // e.g. 10 minutes before
    val enabled: Boolean = true
)

@Entity(
    tableName = "session_logs",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SessionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val date: String, // "YYYY-MM-DD"
    val completedMinutes: Int
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["linkedSessionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subjectId"), Index("linkedSessionId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String = "",
    val subjectId: Long? = null,
    val linkedSessionId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "note_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class NoteTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val text: String,
    val done: Boolean = false
)
