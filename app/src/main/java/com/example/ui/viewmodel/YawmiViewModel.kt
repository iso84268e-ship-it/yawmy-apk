package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NoteWithDetails
import com.example.data.db.SessionWithSubject
import com.example.data.model.Note
import com.example.data.model.NoteTask
import com.example.data.model.Session
import com.example.data.model.SessionLog
import com.example.data.model.SessionType
import com.example.data.model.Subject
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.YawmiRepository
import com.example.notifications.SessionAlarmScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

data class ConflictResult(
    val hasConflict: Boolean,
    val conflictingSessionTitle: String? = null
)

data class PomodoroState(
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val currentSessionNumber: Int = 1,
    val totalSessionsInCycle: Int = 4,
    val linkedSessionId: Long? = null,
    val topicTitle: String = "مذاكرة مركزة",
    val todayFocusMinutes: Int = 0,
    val todayCompletedSessions: Int = 0
)

class YawmiViewModel(
    private val repository: YawmiRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: SessionAlarmScheduler
) : ViewModel() {

    val allSessions: StateFlow<List<SessionWithSubject>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteWithDetails>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<SessionLog>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val weeklyTargetHours: StateFlow<Int> = preferencesRepository.weeklyStudyTarget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    // Current time flow (updates every minute for "الآن" card)
    private val _currentTimeMinutes = MutableStateFlow(getCurrentDayMinutes())
    val currentTimeMinutes: StateFlow<Int> = _currentTimeMinutes.asStateFlow()

    // Focus Mode (Pomodoro) State
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var timerJob: Job? = null

    init {
        // Clock tick
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                _currentTimeMinutes.value = getCurrentDayMinutes()
            }
        }

        // Keep today's focus stats synchronized from room logs (no fake min)
        viewModelScope.launch {
            allLogs.collect { logs ->
                val todayStr = LocalDate.now().toString()
                val todayLogs = logs.filter { it.date == todayStr }
                val totalMins = todayLogs.sumOf { it.completedMinutes }
                val count = todayLogs.size
                _pomodoroState.value = _pomodoroState.value.copy(
                    todayFocusMinutes = totalMins,
                    todayCompletedSessions = count
                )
            }
        }
    }

    private fun getCurrentDayMinutes(): Int {
        val now = Calendar.getInstance()
        return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    }

    fun getCurrentSatDayIndex(): Int {
        val cal = Calendar.getInstance()
        return SessionAlarmScheduler.calendarDayToSatIndex(cal.get(Calendar.DAY_OF_WEEK))
    }

    /**
     * Check if a proposed time conflicts with any active session on common days
     */
    fun checkConflict(
        startMins: Int,
        endMins: Int,
        repeatMask: Int,
        excludeSessionId: Long? = null
    ): ConflictResult {
        val sessions = allSessions.value
        for (item in sessions) {
            val s = item.session
            if (s.id == excludeSessionId || !s.enabled) continue

            // Check if there is overlap in days
            val commonDays = (s.repeatDaysMask and repeatMask)
            if (commonDays != 0) {
                // Check time overlap: (start1 < end2) and (start2 < end1)
                if (startMins < s.endMinutes && s.startMinutes < endMins) {
                    val name = item.subject?.name ?: s.type.labelArabic
                    return ConflictResult(true, "$name (${s.topic})")
                }
            }
        }
        return ConflictResult(false)
    }

    // Session Operations
    fun saveSession(
        session: Session,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (session.id == 0L) {
                val newId = repository.insertSession(session)
                val subject = session.subjectId?.let { repository.getAllSubjects().stateIn(viewModelScope).value.find { s -> s.id == it } }
                alarmScheduler.scheduleSessionAlarm(session.copy(id = newId), subject?.name)
            } else {
                repository.updateSession(session)
                val subject = session.subjectId?.let { repository.getAllSubjects().stateIn(viewModelScope).value.find { s -> s.id == it } }
                alarmScheduler.scheduleSessionAlarm(session, subject?.name)
            }
            onSuccess()
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            alarmScheduler.cancelSessionAlarm(session)
            repository.deleteSession(session)
        }
    }

    fun toggleSessionEnabled(session: Session) {
        viewModelScope.launch {
            val updated = session.copy(enabled = !session.enabled)
            repository.updateSession(updated)
            if (updated.enabled) {
                alarmScheduler.scheduleSessionAlarm(updated, null)
            } else {
                alarmScheduler.cancelSessionAlarm(updated)
            }
        }
    }

    // Note Operations
    fun saveNote(note: Note, tasks: List<NoteTask>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNoteWithTasks(note, tasks)
            } else {
                repository.updateNoteWithTasks(note, tasks)
            }
            onSuccess()
        }
    }

    fun toggleTaskDone(task: NoteTask) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, !task.done)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Subject Operations
    fun addSubject(name: String, colorHex: String = "#46D6A4") {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, colorHex = colorHex))
        }
    }

    // Onboarding Completion with sleep time
    fun completeOnboarding(
        wakeUpMinutes: Int,
        schoolDepartureMinutes: Int,
        schoolStartMinutes: Int,
        schoolEndMinutes: Int,
        sleepMinutes: Int,
        selectedSubjectNames: List<String>
    ) {
        viewModelScope.launch {
            val weekdayMask = 1 or 2 or 4 or 8 or 16 or 32 // Sat..Thu
            val allDays = 127

            repository.insertSession(
                Session(
                    type = SessionType.REMINDER,
                    topic = "الاستيقاظ",
                    startMinutes = wakeUpMinutes,
                    endMinutes = wakeUpMinutes + 30,
                    repeatDaysMask = allDays
                )
            )

            repository.insertSession(
                Session(
                    type = SessionType.REMINDER,
                    topic = "موعد الخروج للمدرسة",
                    startMinutes = schoolDepartureMinutes,
                    endMinutes = schoolDepartureMinutes + 30,
                    repeatDaysMask = weekdayMask
                )
            )

            repository.insertSession(
                Session(
                    type = SessionType.SCHOOL,
                    topic = "المدرسة",
                    startMinutes = schoolStartMinutes,
                    endMinutes = schoolEndMinutes,
                    repeatDaysMask = weekdayMask
                )
            )

            repository.insertSession(
                Session(
                    type = SessionType.REMINDER,
                    topic = "النوم والاسترخاء",
                    startMinutes = sleepMinutes,
                    endMinutes = (sleepMinutes + 30).coerceAtMost(1439),
                    repeatDaysMask = allDays
                )
            )

            selectedSubjectNames.forEachIndexed { index, name ->
                val colors = listOf("#46D6A4", "#7CCBFF", "#FFE3A8", "#CDB8FF")
                repository.insertSubject(Subject(name = name, colorHex = colors[index % colors.size]))
            }

            preferencesRepository.setOnboardingCompleted(true)
            alarmScheduler.rescheduleAllAlarms()
        }
    }

    // Focus Mode (Pomodoro) Controls
    fun startFocusSession(sessionId: Long?, topic: String, durationMinutes: Int = 25) {
        timerJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = true,
            isBreak = false,
            remainingSeconds = durationMinutes * 60,
            totalSeconds = durationMinutes * 60,
            linkedSessionId = sessionId,
            topicTitle = topic.ifBlank { "مذاكرة: الكيمياء · الباب الثاني" }
        )
        runTimer()
    }

    fun setTimerMode(isBreak: Boolean, durationMinutes: Int) {
        timerJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(
            isBreak = isBreak,
            remainingSeconds = durationMinutes * 60,
            totalSeconds = durationMinutes * 60,
            isRunning = false
        )
    }

    fun togglePlayPauseTimer() {
        val current = _pomodoroState.value
        if (current.isRunning) {
            timerJob?.cancel()
            _pomodoroState.value = current.copy(isRunning = false)
        } else {
            _pomodoroState.value = current.copy(isRunning = true)
            runTimer()
        }
    }

    fun resetTimer(durationMinutes: Int = 25) {
        timerJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = false,
            remainingSeconds = durationMinutes * 60,
            totalSeconds = durationMinutes * 60
        )
    }

    fun skipToNextSession() {
        timerJob?.cancel()
        val current = _pomodoroState.value
        val nextSessionNum = if (current.currentSessionNumber >= current.totalSessionsInCycle) 1 else current.currentSessionNumber + 1
        val nextIsBreak = !current.isBreak
        val duration = if (nextIsBreak) 5 else 25
        _pomodoroState.value = current.copy(
            isRunning = false,
            isBreak = nextIsBreak,
            currentSessionNumber = nextSessionNum,
            remainingSeconds = duration * 60,
            totalSeconds = duration * 60
        )
    }

    fun stopTimer() {
        timerJob?.cancel()
        completeFocusSession()
    }

    fun completeFocusSession() {
        timerJob?.cancel()
        val current = _pomodoroState.value
        val completedMinutes = ((current.totalSeconds - current.remainingSeconds) / 60).coerceAtLeast(1)
        val todayStr = LocalDate.now().toString()

        viewModelScope.launch {
            val sessionToLog = current.linkedSessionId ?: allSessions.value.firstOrNull()?.session?.id
            sessionToLog?.let { sId ->
                repository.insertSessionLog(
                    SessionLog(
                        sessionId = sId,
                        date = todayStr,
                        completedMinutes = completedMinutes
                    )
                )
            }
        }

        _pomodoroState.value = current.copy(
            isRunning = false,
            remainingSeconds = current.totalSeconds,
            todayFocusMinutes = current.todayFocusMinutes + completedMinutes,
            todayCompletedSessions = current.todayCompletedSessions + 1
        )
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_pomodoroState.value.remainingSeconds > 0 && _pomodoroState.value.isRunning) {
                delay(1000)
                _pomodoroState.value = _pomodoroState.value.copy(
                    remainingSeconds = _pomodoroState.value.remainingSeconds - 1
                )
            }
            if (_pomodoroState.value.remainingSeconds == 0) {
                completeFocusSession()
            }
        }
    }
}
