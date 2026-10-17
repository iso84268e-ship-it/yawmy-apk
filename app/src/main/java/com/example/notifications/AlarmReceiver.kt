package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra("session_id", -1L)
        val title = intent.getStringExtra("title") ?: "تذكير يومي"
        val message = intent.getStringExtra("message") ?: "لديك موعد قادم وفق جدولك"

        NotificationHelper.showSessionNotification(context, sessionId, title, message)

        // Reschedule for next occurrence
        SessionAlarmScheduler(context).rescheduleAllAlarms()
    }
}
