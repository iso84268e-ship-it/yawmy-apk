package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.NoteWithDetails
import com.example.data.db.SessionWithSubject
import com.example.data.model.Note
import com.example.data.model.NoteTask
import com.example.data.model.Session
import com.example.data.model.SessionLog
import com.example.data.model.SessionType
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class YawmiRepository(private val db: AppDatabase) {

    private val subjectDao = db.subjectDao()
    private val sessionDao = db.sessionDao()
    private val sessionLogDao = db.sessionLogDao()
    private val noteDao = db.noteDao()

    // Subjects
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    // Sessions
    fun getAllSessions(): Flow<List<SessionWithSubject>> = sessionDao.getAllSessionsWithSubject()
    fun getEnabledSessions(): Flow<List<SessionWithSubject>> = sessionDao.getEnabledSessionsWithSubject()
    suspend fun getSessionById(id: Long): SessionWithSubject? = sessionDao.getSessionById(id)
    suspend fun insertSession(session: Session): Long = sessionDao.insertSession(session)
    suspend fun updateSession(session: Session) = sessionDao.updateSession(session)
    suspend fun deleteSession(session: Session) = sessionDao.deleteSession(session)
    suspend fun getAllEnabledSessionsList(): List<Session> = sessionDao.getAllEnabledSessionsList()

    // Notes
    fun getAllNotes(): Flow<List<NoteWithDetails>> = noteDao.getAllNotesWithDetails()
    suspend fun getNoteById(id: Long): NoteWithDetails? = noteDao.getNoteWithDetailsById(id)
    suspend fun insertNoteWithTasks(note: Note, tasks: List<NoteTask>): Long {
        val noteId = noteDao.insertNote(note)
        if (tasks.isNotEmpty()) {
            val updatedTasks = tasks.map { it.copy(noteId = noteId) }
            noteDao.insertTasks(updatedTasks)
        }
        return noteId
    }
    suspend fun updateNoteWithTasks(note: Note, tasks: List<NoteTask>) {
        noteDao.updateNote(note)
        noteDao.deleteTasksForNote(note.id)
        if (tasks.isNotEmpty()) {
            val updatedTasks = tasks.map { it.copy(noteId = note.id) }
            noteDao.insertTasks(updatedTasks)
        }
    }
    suspend fun updateTaskStatus(task: NoteTask, done: Boolean) {
        noteDao.updateTask(task.copy(done = done))
    }
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    // Session Logs
    fun getAllLogs(): Flow<List<SessionLog>> = sessionLogDao.getAllLogs()
    fun getLogsInRange(startDate: String, endDate: String): Flow<List<SessionLog>> =
        sessionLogDao.getLogsInRange(startDate, endDate)
    suspend fun insertSessionLog(log: SessionLog): Long = sessionLogDao.insertLog(log)
}
