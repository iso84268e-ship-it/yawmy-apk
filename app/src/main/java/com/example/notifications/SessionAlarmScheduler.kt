package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.data.model.Session
import com.example.util.formatMinutesToTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class SessionAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleSessionAlarm(session: Session, subjectName: String?) {
        if (!session.enabled || session.repeatDaysMask == 0) {
            cancelSessionAlarm(session)
            return
        }

        // Check exact alarm permission on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If not allowed, we don't crash
                return
            }
        }

        val triggerAtMillis = calculateNextTriggerMillis(session) ?: return

        val title = when {
            !subjectName.isNullOrBlank() -> "${session.type.labelArabic}: $subjectName"
            else -> session.type.labelArabic
        }
        val topicText = if (session.topic.isNotBlank()) " (${session.topic})" else ""
        val timeText = formatMinutesToTime(session.startMinutes)
        val message = "اقترب موعد $title$topicText في $timeText"

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("session_id", session.id)
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            session.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // Handled gracefully if permission revoked
        }
    }

    fun cancelSessionAlarm(session: Session) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            session.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAllAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val sessions = db.sessionDao().getAllEnabledSessionsList()
            for (session in sessions) {
                val subject = session.subjectId?.let { db.subjectDao().getSubjectById(it) }
                scheduleSessionAlarm(session, subject?.name)
            }
        }
    }

    private fun calculateNextTriggerMillis(session: Session): Long? {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val targetMinutes = (session.startMinutes - session.reminderMinutes).coerceAtLeast(0)

        // Try from today up to next 7 days
        for (dayOffset in 0..7) {
            val checkCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val satIndex = calendarDayToSatIndex(checkCal.get(Calendar.DAY_OF_WEEK))
            val isDayEnabled = (session.repeatDaysMask and (1 shl satIndex)) != 0

            if (isDayEnabled) {
                if (dayOffset == 0 && targetMinutes <= currentMinutes) {
                    // Time has passed for today, skip to next day
                    continue
                }
                checkCal.set(Calendar.HOUR_OF_DAY, targetMinutes / 60)
                checkCal.set(Calendar.MINUTE, targetMinutes % 60)
                checkCal.set(Calendar.SECOND, 0)
                checkCal.set(Calendar.MILLISECOND, 0)
                return checkCal.timeInMillis
            }
        }
        return null
    }

    companion object {
        fun calendarDayToSatIndex(calendarDayOfWeek: Int): Int {
            return when (calendarDayOfWeek) {
                Calendar.SATURDAY -> 0
                Calendar.SUNDAY -> 1
                Calendar.MONDAY -> 2
                Calendar.TUESDAY -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 5
                Calendar.FRIDAY -> 6
                else -> 0
            }
        }
    }
}
