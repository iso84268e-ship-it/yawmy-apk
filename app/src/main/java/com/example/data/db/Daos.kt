package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Note
import com.example.data.model.NoteTask
import com.example.data.model.Session
import com.example.data.model.SessionLog
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

data class SessionWithSubject(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: Subject?
)

data class NoteWithDetails(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: Subject?,
    @Relation(
        parentColumn = "linkedSessionId",
        entityColumn = "id"
    )
    val session: Session?,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val tasks: List<NoteTask> = emptyList()
)

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)
}

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM sessions ORDER BY startMinutes ASC")
    fun getAllSessionsWithSubject(): Flow<List<SessionWithSubject>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE enabled = 1 ORDER BY startMinutes ASC")
    fun getEnabledSessionsWithSubject(): Flow<List<SessionWithSubject>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): SessionWithSubject?

    @Query("SELECT * FROM sessions WHERE enabled = 1")
    suspend fun getAllEnabledSessionsList(): List<Session>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<Session>)

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)
}

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs WHERE date >= :startDate AND date <= :endDate")
    fun getLogsInRange(startDate: String, endDate: String): Flow<List<SessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SessionLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<SessionLog>)

    @Query("DELETE FROM session_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteWithDetailsById(id: Long): NoteWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // Task items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: NoteTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<NoteTask>)

    @Update
    suspend fun updateTask(task: NoteTask)

    @Delete
    suspend fun deleteTask(task: NoteTask)

    @Query("DELETE FROM note_tasks WHERE noteId = :noteId")
    suspend fun deleteTasksForNote(noteId: Long)
}
